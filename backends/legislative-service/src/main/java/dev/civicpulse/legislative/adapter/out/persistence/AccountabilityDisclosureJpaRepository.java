package dev.civicpulse.legislative.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface AccountabilityDisclosureJpaRepository extends JpaRepository<AccountabilityDisclosureJpaEntity, UUID> {

  List<AccountabilityDisclosureJpaEntity> findByPoliticianAccountIdOrderBySubmittedAtDesc(UUID politicianAccountId);
}
