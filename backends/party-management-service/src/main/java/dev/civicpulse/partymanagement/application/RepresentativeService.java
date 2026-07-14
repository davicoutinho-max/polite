package dev.civicpulse.partymanagement.application;

import dev.civicpulse.partymanagement.application.port.in.ManageRepresentativeUseCase;
import dev.civicpulse.partymanagement.application.port.out.EventPublisher;
import dev.civicpulse.partymanagement.application.port.out.PartyRepresentativeRepository;
import dev.civicpulse.partymanagement.domain.event.RepresentativeLinked;
import dev.civicpulse.partymanagement.domain.event.RepresentativeRemoved;
import dev.civicpulse.partymanagement.domain.exception.AlreadyRepresentativeException;
import dev.civicpulse.partymanagement.domain.exception.RepresentativeNotFoundException;
import dev.civicpulse.partymanagement.domain.model.PartyRepresentative;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RepresentativeService implements ManageRepresentativeUseCase {

  private final PartyRepresentativeRepository representativeRepository;
  private final EventPublisher eventPublisher;
  private final Clock clock;

  public RepresentativeService(PartyRepresentativeRepository representativeRepository, EventPublisher eventPublisher, Clock clock) {
    this.representativeRepository = representativeRepository;
    this.eventPublisher = eventPublisher;
    this.clock = clock;
  }

  @Override
  @Transactional
  public PartyRepresentative linkExisting(UUID partyId, UUID politicianAccountId, String roleTitle) {
    if (representativeRepository.existsByPartyIdAndPoliticianAccountId(partyId, politicianAccountId)) {
      throw new AlreadyRepresentativeException();
    }
    Instant now = clock.instant();
    PartyRepresentative saved = representativeRepository.save(PartyRepresentative.link(UUID.randomUUID(), partyId, politicianAccountId, roleTitle, now));
    eventPublisher.publish(new RepresentativeLinked(partyId, politicianAccountId, roleTitle, now));
    return saved;
  }

  @Override
  @Transactional
  public void unlink(UUID partyId, UUID politicianAccountId) {
    PartyRepresentative representative =
        representativeRepository
            .findByPartyIdAndPoliticianAccountId(partyId, politicianAccountId)
            .orElseThrow(() -> new RepresentativeNotFoundException(partyId, politicianAccountId));
    representativeRepository.delete(representative.id());
    eventPublisher.publish(new RepresentativeRemoved(partyId, politicianAccountId, clock.instant()));
  }

  @Override
  @Transactional(readOnly = true)
  public List<PartyRepresentative> listByParty(UUID partyId) {
    return representativeRepository.findByPartyId(partyId);
  }
}
