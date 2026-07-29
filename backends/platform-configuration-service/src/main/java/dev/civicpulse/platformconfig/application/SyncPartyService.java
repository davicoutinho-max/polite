package dev.civicpulse.platformconfig.application;

import dev.civicpulse.platformconfig.application.port.in.SyncPartyUseCase;
import dev.civicpulse.platformconfig.application.port.out.EventPublisher;
import dev.civicpulse.platformconfig.application.port.out.IdentityProvisioningGateway;
import dev.civicpulse.platformconfig.application.port.out.PartyRegistryRepository;
import dev.civicpulse.platformconfig.domain.event.PartyNumberCorrected;
import dev.civicpulse.platformconfig.domain.event.PartyRegistered;
import dev.civicpulse.platformconfig.domain.model.PartyRegistryEntry;
import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SyncPartyService implements SyncPartyUseCase {

  private static final Logger log = LoggerFactory.getLogger(SyncPartyService.class);

  // Matches government-sync-service's own PLACEHOLDER_NUMBER_BASE/RANGE (900_000-999_999) — a
  // synthetic number synced there when the source API had no real one on file. Real TSE electoral
  // numbers never come close to this range, so it's a safe, one-sided detector for "this party's
  // stored number isn't real yet" without needing an explicit flag threaded through the sync call.
  private static final int PLACEHOLDER_NUMBER_THRESHOLD = 900_000;

  private final PartyRegistryRepository partyRegistryRepository;
  private final IdentityProvisioningGateway identityProvisioningGateway;
  private final EventPublisher eventPublisher;
  private final Clock clock;

  public SyncPartyService(
      PartyRegistryRepository partyRegistryRepository,
      IdentityProvisioningGateway identityProvisioningGateway,
      EventPublisher eventPublisher,
      Clock clock) {
    this.partyRegistryRepository = partyRegistryRepository;
    this.identityProvisioningGateway = identityProvisioningGateway;
    this.eventPublisher = eventPublisher;
    this.clock = clock;
  }

  @Override
  @Transactional
  public PartyRegistryEntry syncParty(SyncPartyCommand command) {
    // Some TSE-sourced SG_PARTIDO values carry spaces/punctuation (e.g. a coligação-context
    // rendering of "PC do B") that a plain acronym.toLowerCase() would bake into an invalid
    // handle/email — stripped down to alphanumerics only for that purpose; the acronym stored on
    // the registry entry itself (below) is left exactly as submitted.
    String emailSafeAcronym = command.acronym().toLowerCase().replaceAll("[^a-z0-9]", "");

    var existing = partyRegistryRepository.findByAcronym(command.acronym());
    if (existing.isPresent()) {
      // Real parties barely ever rename/rebrand — the acronym match is enough to consider this
      // already-synced; re-provisioning the account still refreshes the logo (avatarUrl) below
      // even though the registry row itself is left untouched.
      identityProvisioningGateway.provisionSyncedPartyAccount(
          command.name(),
          emailSafeAcronym,
          emailSafeAcronym + "@sync.gov.br",
          command.logoUrl(),
          command.documentType(),
          command.rawDocumentNumber(),
          command.externalSource(),
          command.externalId());
      maybeCorrectPlaceholderNumber(existing.get(), command.number());
      return existing.get();
    }

    IdentityProvisioningGateway.ProvisionedAccount account =
        identityProvisioningGateway.provisionSyncedPartyAccount(
            command.name(),
            emailSafeAcronym,
            emailSafeAcronym + "@sync.gov.br",
            command.logoUrl(),
            command.documentType(),
            command.rawDocumentNumber(),
            command.externalSource(),
            command.externalId());

    var now = clock.instant();
    PartyRegistryEntry entry =
        PartyRegistryEntry.register(account.accountId(), command.name(), command.acronym(), command.number(), null, null, now);
    PartyRegistryEntry saved = partyRegistryRepository.save(entry);
    eventPublisher.publish(new PartyRegistered(saved.id(), saved.name(), saved.acronym(), saved.number(), null, null, now));
    return saved;
  }

  /** Best-effort — never fails the sync. Corrects a party that was first registered with a
   * synthetic placeholder number once a call carrying the real one comes through, e.g. TSE's
   * state/municipal sync running after Câmara's federal sync already created the party with a
   * null-numeroEleitoral placeholder. Skips if the "real" number is itself a placeholder (no
   * improvement) or already taken by a different party (never overwrites a real number with a
   * conflicting one). */
  private void maybeCorrectPlaceholderNumber(PartyRegistryEntry existing, int incomingNumber) {
    boolean existingIsPlaceholder = existing.number() >= PLACEHOLDER_NUMBER_THRESHOLD;
    boolean incomingIsReal = incomingNumber < PLACEHOLDER_NUMBER_THRESHOLD;
    if (!existingIsPlaceholder || !incomingIsReal) {
      return;
    }
    if (partyRegistryRepository.existsByNumber(incomingNumber)) {
      log.debug("Skipping number correction for {} — {} is already taken by another party", existing.acronym(), incomingNumber);
      return;
    }
    existing.correctNumber(incomingNumber);
    partyRegistryRepository.save(existing);
    try {
      // Best-effort: a publish hiccup (e.g. a brand-new topic still propagating through the
      // broker) must not fail the whole candidate sync over what's ultimately a cosmetic fix —
      // directory-service's own copy just stays wrong until the next sync run corrects it again.
      eventPublisher.publish(new PartyNumberCorrected(existing.id(), incomingNumber, clock.instant()));
    } catch (Exception e) {
      log.warn("Failed to publish PartyNumberCorrected for {}: {}", existing.acronym(), e.getMessage());
    }
  }
}
