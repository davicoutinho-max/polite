package dev.civicpulse.platformconfig.adapter.out.persistence;

import dev.civicpulse.platformconfig.application.port.out.PoliticianAssignmentRepository;
import dev.civicpulse.platformconfig.domain.model.PoliticianAssignment;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class PoliticianAssignmentRepositoryAdapter implements PoliticianAssignmentRepository {

  private final PoliticianAssignmentJpaRepository jpaRepository;

  PoliticianAssignmentRepositoryAdapter(PoliticianAssignmentJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public PoliticianAssignment save(PoliticianAssignment assignment) {
    var saved =
        jpaRepository.save(
            new PoliticianAssignmentJpaEntity(assignment.politicianAccountId(), assignment.partyId(), assignment.updatedAt()));
    return toDomain(saved);
  }

  @Override
  public Optional<PoliticianAssignment> findById(UUID politicianAccountId) {
    return jpaRepository.findById(politicianAccountId).map(PoliticianAssignmentRepositoryAdapter::toDomain);
  }

  private static PoliticianAssignment toDomain(PoliticianAssignmentJpaEntity entity) {
    return PoliticianAssignment.reconstitute(entity.getPoliticianAccountId(), entity.getPartyId(), entity.getUpdatedAt());
  }
}
