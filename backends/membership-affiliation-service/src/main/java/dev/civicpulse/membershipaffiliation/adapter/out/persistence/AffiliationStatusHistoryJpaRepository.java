package dev.civicpulse.membershipaffiliation.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface AffiliationStatusHistoryJpaRepository extends JpaRepository<AffiliationStatusHistoryJpaEntity, UUID> {

  List<AffiliationStatusHistoryJpaEntity> findByAffiliationId(UUID affiliationId);
}
