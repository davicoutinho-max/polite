package dev.civicpulse.legislative.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface CommitteeMembershipJpaRepository extends JpaRepository<CommitteeMembershipJpaEntity, UUID> {

  List<CommitteeMembershipJpaEntity> findByPoliticianAccountId(UUID politicianAccountId);
}
