package dev.civicpulse.membershipaffiliation.adapter.out.persistence;

import dev.civicpulse.membershipaffiliation.domain.model.FeeStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface MembershipFeeJpaRepository extends JpaRepository<MembershipFeeJpaEntity, UUID> {

  List<MembershipFeeJpaEntity> findByAffiliationId(UUID affiliationId);

  List<MembershipFeeJpaEntity> findByStatusAndDueDateBefore(FeeStatus status, LocalDate date);
}
