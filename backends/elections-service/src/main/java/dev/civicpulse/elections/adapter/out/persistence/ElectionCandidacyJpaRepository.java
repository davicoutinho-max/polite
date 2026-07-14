package dev.civicpulse.elections.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface ElectionCandidacyJpaRepository extends JpaRepository<ElectionCandidacyJpaEntity, ElectionCandidacyId> {

  List<ElectionCandidacyJpaEntity> findByElectionId(UUID electionId);

  boolean existsByElectionIdAndPoliticianAccountId(UUID electionId, UUID politicianAccountId);
}
