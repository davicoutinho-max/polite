package dev.civicpulse.fundraising.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface ContributionJpaRepository extends JpaRepository<ContributionJpaEntity, UUID> {

  List<ContributionJpaEntity> findByFundraiserIdOrderByCreatedAtAsc(UUID fundraiserId);

  boolean existsByPaymentIntentId(UUID paymentIntentId);
}
