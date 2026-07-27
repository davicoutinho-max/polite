package dev.civicpulse.elections.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface ElectionResultJpaRepository extends JpaRepository<ElectionResultJpaEntity, UUID> {

  List<ElectionResultJpaEntity> findByElectionIdOrderByOfficeAscRankAsc(UUID electionId);

  void deleteByElectionIdAndOffice(UUID electionId, String office);
}
