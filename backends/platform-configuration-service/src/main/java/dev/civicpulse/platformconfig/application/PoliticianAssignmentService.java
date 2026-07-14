package dev.civicpulse.platformconfig.application;

import dev.civicpulse.platformconfig.application.port.in.ManagePoliticianAssignmentUseCase;
import dev.civicpulse.platformconfig.application.port.out.EventPublisher;
import dev.civicpulse.platformconfig.application.port.out.PoliticianAssignmentRepository;
import dev.civicpulse.platformconfig.domain.event.PoliticianReassigned;
import dev.civicpulse.platformconfig.domain.exception.PoliticianAssignmentNotFoundException;
import dev.civicpulse.platformconfig.domain.model.PoliticianAssignment;
import java.time.Clock;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PoliticianAssignmentService implements ManagePoliticianAssignmentUseCase {

  private final PoliticianAssignmentRepository assignmentRepository;
  private final EventPublisher eventPublisher;
  private final Clock clock;

  public PoliticianAssignmentService(PoliticianAssignmentRepository assignmentRepository, EventPublisher eventPublisher, Clock clock) {
    this.assignmentRepository = assignmentRepository;
    this.eventPublisher = eventPublisher;
    this.clock = clock;
  }

  @Override
  @Transactional
  public void onPoliticianRegistered(UUID politicianAccountId, UUID partyId) {
    if (assignmentRepository.findById(politicianAccountId).isPresent()) {
      return; // idempotent — already assigned (e.g. reprocessed message)
    }
    assignmentRepository.save(PoliticianAssignment.assign(politicianAccountId, partyId, clock.instant()));
  }

  @Override
  @Transactional
  public PoliticianAssignment reassign(UUID politicianAccountId, UUID newPartyId) {
    var now = clock.instant();
    PoliticianAssignment assignment =
        assignmentRepository.findById(politicianAccountId).orElseGet(() -> PoliticianAssignment.assign(politicianAccountId, newPartyId, now));
    assignment.reassign(newPartyId, now);
    PoliticianAssignment saved = assignmentRepository.save(assignment);
    eventPublisher.publish(new PoliticianReassigned(politicianAccountId, newPartyId, now));
    return saved;
  }

  @Override
  @Transactional(readOnly = true)
  public PoliticianAssignment getAssignment(UUID politicianAccountId) {
    return assignmentRepository.findById(politicianAccountId).orElseThrow(() -> new PoliticianAssignmentNotFoundException(politicianAccountId));
  }
}
