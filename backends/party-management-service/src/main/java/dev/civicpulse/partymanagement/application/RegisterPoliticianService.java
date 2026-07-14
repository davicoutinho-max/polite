package dev.civicpulse.partymanagement.application;

import dev.civicpulse.partymanagement.application.port.in.RegisterPoliticianUseCase;
import dev.civicpulse.partymanagement.application.port.out.EventPublisher;
import dev.civicpulse.partymanagement.application.port.out.IdentityProvisioningGateway;
import dev.civicpulse.partymanagement.application.port.out.PartyRepresentativeRepository;
import dev.civicpulse.partymanagement.domain.event.PoliticianRegistered;
import dev.civicpulse.partymanagement.domain.event.RepresentativeLinked;
import dev.civicpulse.partymanagement.domain.model.PartyRepresentative;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterPoliticianService implements RegisterPoliticianUseCase {

  private final IdentityProvisioningGateway identityProvisioningGateway;
  private final PartyRepresentativeRepository representativeRepository;
  private final EventPublisher eventPublisher;
  private final Clock clock;

  public RegisterPoliticianService(
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
  public PartyRepresentative registerPolitician(UUID partyId, RegisterPoliticianCommand command) {
    // Not @Transactional-safe against a partial failure (account created in Identity but the
    // local link fails to commit) — acceptable for now since this mirrors the same
    // saga-less trade-off already documented for cross-service calls in
    // docs/architecture/system-architecture.html; a production build would use the outbox
    // pattern here the same way Payments does for its own cross-service consistency.
    IdentityProvisioningGateway.ProvisionedAccount account =
        identityProvisioningGateway.provisionPoliticianAccount(
            command.name(), command.handle(), command.email(), command.rawPassword(), command.documentType(), command.rawDocumentNumber());

    Instant now = clock.instant();
    PartyRepresentative representative =
        PartyRepresentative.link(UUID.randomUUID(), partyId, account.accountId(), command.roleTitle(), now);
    PartyRepresentative saved = representativeRepository.save(representative);

    eventPublisher.publish(new PoliticianRegistered(account.accountId(), partyId, null, null, now));
    eventPublisher.publish(new RepresentativeLinked(partyId, account.accountId(), command.roleTitle(), now));

    return saved;
  }
}
