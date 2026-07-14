package dev.civicpulse.platformconfig.application;

import dev.civicpulse.platformconfig.application.port.in.RegisterPartyUseCase;
import dev.civicpulse.platformconfig.application.port.out.EventPublisher;
import dev.civicpulse.platformconfig.application.port.out.PartyRegistryRepository;
import dev.civicpulse.platformconfig.domain.event.PartyRegistered;
import dev.civicpulse.platformconfig.domain.exception.DuplicatePartyRegistrationException;
import dev.civicpulse.platformconfig.domain.model.PartyRegistryEntry;
import java.time.Clock;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterPartyService implements RegisterPartyUseCase {

  private final PartyRegistryRepository partyRegistryRepository;
  private final EventPublisher eventPublisher;
  private final Clock clock;

  public RegisterPartyService(PartyRegistryRepository partyRegistryRepository, EventPublisher eventPublisher, Clock clock) {
    this.partyRegistryRepository = partyRegistryRepository;
    this.eventPublisher = eventPublisher;
    this.clock = clock;
  }

  @Override
  @Transactional
  public PartyRegistryEntry registerParty(String name, String acronym, int number, String president, String ideology) {
    if (partyRegistryRepository.existsByAcronym(acronym)) {
      throw new DuplicatePartyRegistrationException("acronym");
    }
    if (partyRegistryRepository.existsByNumber(number)) {
      throw new DuplicatePartyRegistrationException("number");
    }
    var now = clock.instant();
    PartyRegistryEntry entry = PartyRegistryEntry.register(UUID.randomUUID(), name, acronym, number, president, ideology, now);
    PartyRegistryEntry saved = partyRegistryRepository.save(entry);
    eventPublisher.publish(new PartyRegistered(saved.id(), saved.name(), saved.acronym(), saved.number(), president, ideology, now));
    return saved;
  }
}
