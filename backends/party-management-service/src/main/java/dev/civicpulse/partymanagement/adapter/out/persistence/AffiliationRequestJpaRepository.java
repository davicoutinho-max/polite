package dev.civicpulse.partymanagement.adapter.out.persistence;

import dev.civicpulse.partymanagement.domain.model.AffiliationRequestStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface AffiliationRequestJpaRepository extends JpaRepository<AffiliationRequestJpaEntity, UUID> {

  List<AffiliationRequestJpaEntity> findByPartyIdAndStatus(UUID partyId, AffiliationRequestStatus status);
}
