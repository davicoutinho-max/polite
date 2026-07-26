package dev.civicpulse.governmentsync.application;

import dev.civicpulse.governmentsync.application.port.in.SyncStateAndMunicipalUseCase;
import dev.civicpulse.governmentsync.application.port.out.PartySyncGateway;
import dev.civicpulse.governmentsync.application.port.out.PartySyncGateway.SyncPartyCommand;
import dev.civicpulse.governmentsync.application.port.out.PoliticianSyncGateway;
import dev.civicpulse.governmentsync.application.port.out.PoliticianSyncGateway.SyncPoliticianCommand;
import dev.civicpulse.governmentsync.application.port.out.TseElectionDataGateway;
import dev.civicpulse.governmentsync.application.port.out.TseElectionDataGateway.TseElectedCandidate;
import dev.civicpulse.governmentsync.application.support.DocumentNumberFallback;
import dev.civicpulse.governmentsync.application.support.HandleSlugGenerator;
import dev.civicpulse.governmentsync.application.support.PortugueseNameCasing;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Orchestrates the state/municipal sync from TSE's bulk election-results dataset (see
 * TseElectionDataGateway) — the same idempotent {@code /politicians/sync}/{@code /parties/sync}
 * infrastructure Phase 1's federal sync uses, just fed from a different, much larger and much
 * less fresh (per-election-cycle, not live) data source.
 *
 * <p><b>Known, source-level limitations, not bugs in this orchestration</b> (documented instead of
 * silently hidden): (1) "Vice-Governador"/"Vice-Prefeito" never appear in this dataset at all —
 * confirmed via live samples (2022 Sergipe/DF, 2024 Sergipe) — because running mates receive no
 * separate nominal votes, so this sync cannot populate them from this source; (2) the 2022 general
 * election is the most recent state-level data available as of this writing — a new state election
 * lands in October 2026 and {@code STATE_ELECTION_YEAR} will need bumping once TSE publishes those
 * results; (3) no CPF/photo/email exists anywhere in this dataset, so every TSE-sourced politician
 * gets the same synthetic-document/synthetic-email treatment Senado's senators already need in
 * Phase 1 — meaning the account-claim flow (identity-service's {@code Account.claim}) can never
 * actually trigger for a TSE-sourced profile today, only for Câmara-sourced ones with a real CPF
 * on file. */
@Service
public class SyncStateAndMunicipalService implements SyncStateAndMunicipalUseCase {

  private static final Logger log = LoggerFactory.getLogger(SyncStateAndMunicipalService.class);

  private static final int STATE_ELECTION_YEAR = 2022;
  private static final int MUNICIPAL_ELECTION_YEAR = 2024;
  private static final Set<String> STATE_CARGOS = Set.of("Governador", "Deputado Estadual", "Deputado Distrital");
  private static final Set<String> MUNICIPAL_CARGOS = Set.of("Prefeito", "Vereador");
  private static final int PLACEHOLDER_NUMBER_BASE = 900_000;
  private static final int PLACEHOLDER_NUMBER_RANGE = 100_000;

  private final TseElectionDataGateway tseElectionDataGateway;
  private final PartySyncGateway partySyncGateway;
  private final PoliticianSyncGateway politicianSyncGateway;

  public SyncStateAndMunicipalService(
      TseElectionDataGateway tseElectionDataGateway, PartySyncGateway partySyncGateway, PoliticianSyncGateway politicianSyncGateway) {
    this.tseElectionDataGateway = tseElectionDataGateway;
    this.partySyncGateway = partySyncGateway;
    this.politicianSyncGateway = politicianSyncGateway;
  }

  @Override
  public SyncResult syncStateAndMunicipal(String uf) {
    Map<String, UUID> partyIdByAcronym = new HashMap<>();
    int[] partiesSynced = {0};
    int failures = 0;

    int stateSynced = 0;
    for (TseElectedCandidate candidate : tseElectionDataGateway.fetchElectedCandidates(STATE_ELECTION_YEAR, uf, STATE_CARGOS)) {
      try {
        syncCandidate(candidate, "state", candidate.uf(), "TSE_ESTADUAL", partyIdByAcronym, partiesSynced);
        stateSynced++;
      } catch (Exception e) {
        log.warn("Failed to sync state-level candidate {} ({}): {}", candidate.ballotName(), candidate.externalId(), e.getMessage());
        failures++;
      }
    }

    int municipalSynced = 0;
    for (TseElectedCandidate candidate : tseElectionDataGateway.fetchElectedCandidates(MUNICIPAL_ELECTION_YEAR, uf, MUNICIPAL_CARGOS)) {
      try {
        syncCandidate(candidate, "municipal", candidate.municipality(), "TSE_MUNICIPAL", partyIdByAcronym, partiesSynced);
        municipalSynced++;
      } catch (Exception e) {
        log.warn("Failed to sync municipal-level candidate {} ({}): {}", candidate.ballotName(), candidate.externalId(), e.getMessage());
        failures++;
      }
    }

    log.info(
        "State/municipal sync complete for {}: {} parties, {} state-level, {} municipal-level, {} failures",
        uf, partiesSynced[0], stateSynced, municipalSynced, failures);
    return new SyncResult(partiesSynced[0], stateSynced, municipalSynced, failures);
  }

  private void syncCandidate(
      TseElectedCandidate candidate, String govLevel, String location, String externalSource, Map<String, UUID> partyIdByAcronym, int[] partiesSynced) {
    String acronymKey = candidate.partyAcronym() == null ? "" : candidate.partyAcronym().toUpperCase();
    UUID partyId = partyIdByAcronym.get(acronymKey);
    if (partyId == null) {
      partyId = syncParty(candidate);
      partyIdByAcronym.put(acronymKey, partyId);
      partiesSynced[0]++;
    }

    String rawName = candidate.ballotName() != null && !candidate.ballotName().isBlank() ? candidate.ballotName() : candidate.legalName();
    String name = PortugueseNameCasing.titleCase(rawName);
    String documentNumber = DocumentNumberFallback.synthesize(externalSource + ":" + candidate.externalId(), 11);
    String email = "tse" + candidate.externalId() + "@sync.gov.br";

    politicianSyncGateway.syncPolitician(
        partyId,
        new SyncPoliticianCommand(
            name,
            HandleSlugGenerator.slugify(name, "tse-" + candidate.externalId()),
            email,
            null,
            documentNumber,
            externalSource,
            candidate.externalId(),
            candidate.cargo(),
            location,
            govLevel));
  }

  private UUID syncParty(TseElectedCandidate candidate) {
    int number =
        candidate.partyNumber() != null
            ? candidate.partyNumber()
            : DocumentNumberFallback.syntheticNumber("TSE_PARTIDO:" + candidate.partyAcronym(), PLACEHOLDER_NUMBER_BASE, PLACEHOLDER_NUMBER_RANGE);
    String documentNumber = DocumentNumberFallback.synthesize("TSE_PARTIDO:" + candidate.partyAcronym(), 14);
    return partySyncGateway.syncParty(
        new SyncPartyCommand(candidate.partyName(), candidate.partyAcronym(), number, null, documentNumber, "TSE_PARTIDO", candidate.partyAcronym()));
  }
}
