package dev.civicpulse.platformconfig.adapter.out.persistence;

import dev.civicpulse.platformconfig.application.port.out.PoliticalPositionRepository;
import dev.civicpulse.platformconfig.domain.model.PoliticalPosition;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class PoliticalPositionRepositoryAdapter implements PoliticalPositionRepository {

  private final PoliticalPositionJpaRepository jpaRepository;

  PoliticalPositionRepositoryAdapter(PoliticalPositionJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public PoliticalPosition save(PoliticalPosition position) {
    var saved =
        jpaRepository.save(new PoliticalPositionJpaEntity(position.id(), position.name(), (short) position.sortOrder()));
    return toDomain(saved);
  }

  @Override
  public void delete(UUID id) {
    jpaRepository.deleteById(id);
  }

  @Override
  public List<PoliticalPosition> findAllOrderBySortOrder() {
    return jpaRepository.findAllByOrderBySortOrder().stream().map(PoliticalPositionRepositoryAdapter::toDomain).toList();
  }

  private static PoliticalPosition toDomain(PoliticalPositionJpaEntity entity) {
    return PoliticalPosition.reconstitute(entity.getId(), entity.getName(), entity.getSortOrder());
  }
}
