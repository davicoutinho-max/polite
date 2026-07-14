package dev.civicpulse.platformconfig.application;

import dev.civicpulse.platformconfig.application.port.in.RegisterPartyUseCase;
import dev.civicpulse.platformconfig.application.port.out.EventPublisher;
import dev.civicpulse.platformconfig.application.port.out.IdentityProvisioningGateway;
import dev.civicpulse.platformconfig.application.port.out.PartyRegistryRepository;
import dev.civicpulse.platformconfig.domain.event.PartyRegistered;
import dev.civicpulse.platformconfig.domain.exception.DuplicatePartyRegistrationException;
import dev.civicpulse.platformconfig.domain.model.PartyRegistryEntry;
import java.time.Clock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterPartyService implements RegisterPartyUseCase {

  private final PartyRegistryRepository partyRegistryRepository;
  private final IdentityProvisioningGateway identityProvisioningGateway;
  private final EventPublisher eventPublisher;
  private final Clock clock;

  public RegisterPartyService(
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
  public PartyRegistryEntry registerParty(
      String name,
      String acronym,
      int number,
      String president,
      String ideology,
      String handle,
      String email,
      String rawPassword,
      String documentType,
      String rawDocumentNumber) {
    if (partyRegistryRepository.existsByAcronym(acronym)) {
      throw new DuplicatePartyRegistrationException("acronym");
    }
    if (partyRegistryRepository.existsByNumber(number)) {
      throw new DuplicatePartyRegistrationException("number");
    }

    // Not @Transactional-safe against a partial failure (account created in Identity but the
    // local registry row fails to commit) — same accepted trade-off already documented in
    // party-management-service's RegisterPoliticianService for the identical pattern.
    IdentityProvisioningGateway.ProvisionedAccount account =
        identityProvisioningGateway.provisionPartyAccount(name, handle, email, rawPassword, documentType, rawDocumentNumber);

    var now = clock.instant();
    PartyRegistryEntry entry = PartyRegistryEntry.register(account.accountId(), name, acronym, number, president, ideology, now);
    PartyRegistryEntry saved = partyRegistryRepository.save(entry);
    eventPublisher.publish(new PartyRegistered(saved.id(), saved.name(), saved.acronym(), saved.number(), president, ideology, now));
    return saved;
  }
}
