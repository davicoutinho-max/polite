package dev.civicpulse.governmentsync.application;

import dev.civicpulse.governmentsync.application.port.in.SyncFederalLegislatureUseCase;
import dev.civicpulse.governmentsync.application.port.out.CamaraGateway;
import dev.civicpulse.governmentsync.application.port.out.CamaraGateway.CamaraDeputy;
import dev.civicpulse.governmentsync.application.port.out.CamaraGateway.CamaraParty;
import dev.civicpulse.governmentsync.application.port.out.LegislativeDossierGateway;
import dev.civicpulse.governmentsync.application.port.out.PartySyncGateway;
import dev.civicpulse.governmentsync.application.port.out.PartySyncGateway.SyncPartyCommand;
import dev.civicpulse.governmentsync.application.port.out.PoliticianSyncGateway;
import dev.civicpulse.governmentsync.application.port.out.PoliticianSyncGateway.SyncPoliticianCommand;
import dev.civicpulse.governmentsync.application.port.out.SenadoGateway;
import dev.civicpulse.governmentsync.application.port.out.SenadoGateway.SenadoSenator;
import dev.civicpulse.governmentsync.application.support.DocumentNumberFallback;
import dev.civicpulse.governmentsync.application.support.HandleSlugGenerator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Orchestrates the daily federal sync: parties first (so politicians always have a party id to
 * link against), then deputies, then senators. Per-record failures (a single bad API response, a
 * transient network blip) are caught and counted rather than aborting the whole run — with ~600
 * remote records synced one at a time, losing the entire night's run over one bad record would be
 * worse than a handful of politicians staying stale until the next run. */
@Service
public class SyncFederalLegislatureService implements SyncFederalLegislatureUseCase {

  private static final Logger log = LoggerFactory.getLogger(SyncFederalLegislatureService.class);

  // Real TSE electoral numbers for parties are small (two/three digits). Placeholder numbers used
  // when the Câmara API doesn't supply a real one (confirmed null even for well-known parties) are
  // pushed into a high, wide range so they can never collide with — or be mistaken for — a real
  // one, and so that the ~30-party candidate pool collides with itself only very rarely.
  private static final int PLACEHOLDER_NUMBER_BASE = 900_000;
  private static final int PLACEHOLDER_NUMBER_RANGE = 100_000;

  private final CamaraGateway camaraGateway;
  private final SenadoGateway senadoGateway;
  private final PartySyncGateway partySyncGateway;
  private final PoliticianSyncGateway politicianSyncGateway;
  private final LegislativeDossierGateway legislativeDossierGateway;

  public SyncFederalLegislatureService(
      CamaraGateway camaraGateway,
      SenadoGateway senadoGateway,
      PartySyncGateway partySyncGateway,
      PoliticianSyncGateway politicianSyncGateway,
      LegislativeDossierGateway legislativeDossierGateway) {
    this.camaraGateway = camaraGateway;
    this.senadoGateway = senadoGateway;
    this.partySyncGateway = partySyncGateway;
    this.politicianSyncGateway = politicianSyncGateway;
    this.legislativeDossierGateway = legislativeDossierGateway;
  }

  @Override
  public SyncResult syncFederalLegislature() {
    Map<String, UUID> partyIdByAcronym = new HashMap<>();
    int partiesSynced = 0;
    int failures = 0;

    for (CamaraParty party : camaraGateway.fetchAllParties()) {
      try {
        UUID partyId =
            syncParty(party.acronym(), party.name(), party.electoralNumber(), party.logoUrl(), "CAMARA_PARTIDO", party.externalId());
        partyIdByAcronym.put(party.acronym().toUpperCase(), partyId);
        partiesSynced++;
      } catch (Exception e) {
        log.warn("Failed to sync party {}: {}", party.acronym(), e.getMessage());
        failures++;
      }
    }

    int deputiesSynced = 0;
    for (CamaraDeputy deputy : camaraGateway.fetchAllDeputies()) {
      try {
        UUID partyId = partyIdByAcronym.get(deputy.partyAcronym() == null ? null : deputy.partyAcronym().toUpperCase());
        if (partyId == null) {
          log.warn("Deputy {} references unknown party {} — skipping", deputy.name(), deputy.partyAcronym());
          failures++;
          continue;
        }
        String cpfDigits = digitsOnly(deputy.cpf());
        String documentNumber =
            cpfDigits.length() == 11 ? cpfDigits : DocumentNumberFallback.synthesize("CAMARA_DEPUTADO:" + deputy.externalId(), 11);
        String email = deputy.email() != null ? deputy.email() : "dep" + deputy.externalId() + "@sync.gov.br";
        UUID politicianAccountId =
            politicianSyncGateway.syncPolitician(
                partyId,
                new SyncPoliticianCommand(
                    deputy.name(),
                    HandleSlugGenerator.slugify(deputy.name(), "dep-" + deputy.externalId()),
                    email,
                    deputy.photoUrl(),
                    documentNumber,
                    "CAMARA_DEPUTADO",
                    deputy.externalId(),
                    "Deputado Federal",
                    deputy.state(),
                    "federal"));
        // Best-effort, see LegislativeDossierGateway's javadoc — never counted as a sync failure.
        legislativeDossierGateway.enrichDossier(politicianAccountId, deputy.education(), deputy.email());
        legislativeDossierGateway.addSocialLinks(politicianAccountId, deputy.socialLinks());
        deputiesSynced++;
      } catch (Exception e) {
        log.warn("Failed to sync deputy {}: {}", deputy.name(), e.getMessage());
        failures++;
      }
    }

    int senatorsSynced = 0;
    for (SenadoSenator senator : senadoGateway.fetchCurrentSenators()) {
      try {
        String acronymKey = senator.partyAcronym() == null ? null : senator.partyAcronym().toUpperCase();
        UUID partyId = partyIdByAcronym.get(acronymKey);
        if (partyId == null) {
          // A handful of small parties hold Senate seats without ever having a Chamber deputy —
          // Senado's API gives only the acronym, not a full name/logo, so this fallback
          // registration is deliberately thinner than the Câmara-sourced ones above.
          partyId = syncParty(senator.partyAcronym(), senator.partyAcronym(), null, null, "SENADO_PARTIDO_FALLBACK", senator.partyAcronym());
          partyIdByAcronym.put(acronymKey, partyId);
          partiesSynced++;
        }
        String documentNumber = DocumentNumberFallback.synthesize("SENADO_SENADOR:" + senator.externalId(), 11);
        String email = senator.email() != null ? senator.email() : "sen" + senator.externalId() + "@sync.gov.br";
        politicianSyncGateway.syncPolitician(
            partyId,
            new SyncPoliticianCommand(
                senator.name(),
                HandleSlugGenerator.slugify(senator.name(), "sen-" + senator.externalId()),
                email,
                senator.photoUrl(),
                documentNumber,
                "SENADO_SENADOR",
                senator.externalId(),
                "Senador",
                senator.state(),
                "federal"));
        senatorsSynced++;
      } catch (Exception e) {
        log.warn("Failed to sync senator {}: {}", senator.name(), e.getMessage());
        failures++;
      }
    }

    log.info(
        "Federal sync complete: {} parties, {} deputies, {} senators, {} failures",
        partiesSynced, deputiesSynced, senatorsSynced, failures);
    return new SyncResult(partiesSynced, deputiesSynced, senatorsSynced, failures);
  }

  private UUID syncParty(String acronym, String name, Integer electoralNumber, String logoUrl, String externalSource, String externalId) {
    int number = electoralNumber != null ? electoralNumber : placeholderNumber(externalSource + ":" + externalId);
    String documentNumber = DocumentNumberFallback.synthesize(externalSource + ":" + externalId, 14);
    return partySyncGateway.syncParty(new SyncPartyCommand(name, acronym, number, logoUrl, documentNumber, externalSource, externalId));
  }

  private static int placeholderNumber(String seed) {
    return DocumentNumberFallback.syntheticNumber(seed, PLACEHOLDER_NUMBER_BASE, PLACEHOLDER_NUMBER_RANGE);
  }

  private static String digitsOnly(String value) {
    return value == null ? "" : value.replaceAll("\\D", "");
  }
}
