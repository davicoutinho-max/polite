package dev.civicpulse.platformconfig.application;

import dev.civicpulse.platformconfig.application.port.in.SyncPartyUseCase;
import dev.civicpulse.platformconfig.application.port.out.EventPublisher;
import dev.civicpulse.platformconfig.application.port.out.IdentityProvisioningGateway;
import dev.civicpulse.platformconfig.application.port.out.PartyRegistryRepository;
import dev.civicpulse.platformconfig.domain.event.PartyRegistered;
import dev.civicpulse.platformconfig.domain.model.PartyRegistryEntry;
import java.time.Clock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SyncPartyService implements SyncPartyUseCase {

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
}
