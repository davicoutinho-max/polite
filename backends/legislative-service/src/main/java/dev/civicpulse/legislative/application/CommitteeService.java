package dev.civicpulse.legislative.application;

import dev.civicpulse.legislative.application.port.in.CommitteeUseCase;
import dev.civicpulse.legislative.application.port.out.CommitteeMembershipRepository;
import dev.civicpulse.legislative.application.port.out.LegislativeEventPublisher;
import dev.civicpulse.legislative.domain.event.CommitteeMembershipChanged;
import dev.civicpulse.legislative.domain.model.CommitteeKind;
import dev.civicpulse.legislative.domain.model.CommitteeMembership;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommitteeService implements CommitteeUseCase {

  private final CommitteeMembershipRepository committeeRepository;
  private final LegislativeEventPublisher eventPublisher;
  private final Clock clock;

  public CommitteeService(CommitteeMembershipRepository committeeRepository, LegislativeEventPublisher eventPublisher, Clock clock) {
    this.committeeRepository = committeeRepository;
    this.eventPublisher = eventPublisher;
    this.clock = clock;
  }

  @Override
  @Transactional
  public CommitteeMembership joinCommittee(UUID politicianAccountId, String name, String role, CommitteeKind kind) {
    CommitteeMembership saved = committeeRepository.save(CommitteeMembership.join(politicianAccountId, name, role, kind));
    eventPublisher.publish(
        new CommitteeMembershipChanged(saved.id().orElseThrow(), politicianAccountId, name, clock.instant()));
    return saved;
  }

  @Override
  public List<CommitteeMembership> getCommittees(UUID politicianAccountId) {
    return committeeRepository.findByPolitician(politicianAccountId);
  }
}
