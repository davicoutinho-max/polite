package dev.civicpulse.legislative.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface VoteRecordJpaRepository extends JpaRepository<VoteRecordJpaEntity, UUID> {

  List<VoteRecordJpaEntity> findByPoliticianAccountIdOrderByVoteDateDesc(UUID politicianAccountId);
}
