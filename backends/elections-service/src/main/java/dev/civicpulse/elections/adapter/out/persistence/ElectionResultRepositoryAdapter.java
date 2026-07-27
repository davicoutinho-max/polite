package dev.civicpulse.elections.adapter.out.persistence;

import dev.civicpulse.elections.application.port.out.ElectionResultRepository;
import dev.civicpulse.elections.domain.model.ElectionResult;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class ElectionResultRepositoryAdapter implements ElectionResultRepository {

  private final ElectionResultJpaRepository jpaRepository;

  ElectionResultRepositoryAdapter(ElectionResultJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  @Transactional
  public void replaceForOffice(UUID electionId, String office, List<ElectionResult> results) {
    // Hibernate flushes inserts before deletes within a single flush regardless of call order —
    // without this explicit flush, inserting a row that reuses a natural key (election_id,
    // office, external_id) about to be deleted would hit the unique index before the delete lands.
    jpaRepository.deleteByElectionIdAndOffice(electionId, office);
    jpaRepository.flush();
    jpaRepository.saveAll(results.stream().map(ElectionResultMapper::toEntity).toList());
  }

  @Override
  public List<ElectionResult> findByElectionId(UUID electionId) {
    return jpaRepository.findByElectionIdOrderByOfficeAscRankAsc(electionId).stream().map(ElectionResultMapper::toDomain).toList();
  }
}
