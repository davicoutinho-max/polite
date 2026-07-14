package dev.civicpulse.membershipaffiliation.adapter.out.persistence;

import dev.civicpulse.membershipaffiliation.application.port.out.AffiliationStatusHistoryRepository;
import dev.civicpulse.membershipaffiliation.domain.model.AffiliationStatusHistoryEntry;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class AffiliationStatusHistoryRepositoryAdapter implements AffiliationStatusHistoryRepository {

  private final AffiliationStatusHistoryJpaRepository jpaRepository;

  AffiliationStatusHistoryRepositoryAdapter(AffiliationStatusHistoryJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public AffiliationStatusHistoryEntry save(AffiliationStatusHistoryEntry entry) {
    var saved =
        jpaRepository.save(
            new AffiliationStatusHistoryJpaEntity(
                entry.id(), entry.affiliationId(), entry.fromStatus().orElse(null), entry.toStatus(), entry.changedBy(), entry.changedAt()));
    return toDomain(saved);
  }

  @Override
  public List<AffiliationStatusHistoryEntry> findByAffiliationId(UUID affiliationId) {
    return jpaRepository.findByAffiliationId(affiliationId).stream().map(AffiliationStatusHistoryRepositoryAdapter::toDomain).toList();
  }

  private static AffiliationStatusHistoryEntry toDomain(AffiliationStatusHistoryJpaEntity entity) {
    return AffiliationStatusHistoryEntry.reconstitute(
        entity.getId(), entity.getAffiliationId(), entity.getFromStatus(), entity.getToStatus(), entity.getChangedBy(), entity.getChangedAt());
  }
}
