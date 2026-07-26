package dev.civicpulse.partymanagement.application;

import dev.civicpulse.partymanagement.application.port.in.SyncPoliticianUseCase;
import dev.civicpulse.partymanagement.application.port.out.EventPublisher;
import dev.civicpulse.partymanagement.application.port.out.IdentityProvisioningGateway;
import dev.civicpulse.partymanagement.application.port.out.PartyRepresentativeRepository;
import dev.civicpulse.partymanagement.domain.event.RepresentativeLinked;
import dev.civicpulse.partymanagement.domain.event.RepresentativeRemoved;
import dev.civicpulse.partymanagement.domain.model.PartyRepresentative;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SyncPoliticianService implements SyncPoliticianUseCase {

  private final IdentityProvisioningGateway identityProvisioningGateway;
  private final PartyRepresentativeRepository representativeRepository;
  private final EventPublisher eventPublisher;
  private final Clock clock;

  public SyncPoliticianService(
      IdentityProvisioningGateway identityProvisioningGateway,
      PartyRepresentativeRepository representativeRepository,
      EventPublisher eventPublisher,
      Clock clock) {
    this.identityProvisioningGateway = identityProvisioningGateway;
    this.representativeRepository = representativeRepository;
    this.eventPublisher = eventPublisher;
    this.clock = clock;
  }

  @Override
  @Transactional
  public PartyRepresentative syncPolitician(UUID partyId, SyncPoliticianCommand command) {
    IdentityProvisioningGateway.ProvisionedAccount account =
        identityProvisioningGateway.provisionSyncedPoliticianAccount(
            command.name(),
            command.handle(),
            command.email(),
            command.avatarUrl(),
            command.documentType(),
            command.rawDocumentNumber(),
            command.externalSource(),
            command.externalId());

    Instant now = clock.instant();
    var existing = representativeRepository.findByPoliticianAccountId(account.accountId());

    if (existing.isEmpty()) {
      return linkNew(partyId, account.accountId(), command, now);
    }

    PartyRepresentative current = existing.get();
    if (current.partyId().equals(partyId)) {
      // Already correctly linked — role/state may have changed (e.g. re-elected under a new
      // title) but that's cosmetic enough to leave as a future improvement; the important
      // invariant (which party) already holds.
      return current;
    }

    // Party switch ("troca de partido") — remove the stale link, then link fresh.
    representativeRepository.delete(current.id());
    eventPublisher.publish(new RepresentativeRemoved(current.partyId(), account.accountId(), now));
    return linkNew(partyId, account.accountId(), command, now);
  }

  private PartyRepresentative linkNew(UUID partyId, UUID politicianAccountId, SyncPoliticianCommand command, Instant now) {
    PartyRepresentative saved =
        representativeRepository.save(PartyRepresentative.link(UUID.randomUUID(), partyId, politicianAccountId, command.roleTitle(), now));
    eventPublisher.publish(
        new RepresentativeLinked(partyId, politicianAccountId, command.roleTitle(), command.state(), command.govLevel(), now));
    return saved;
  }
}
