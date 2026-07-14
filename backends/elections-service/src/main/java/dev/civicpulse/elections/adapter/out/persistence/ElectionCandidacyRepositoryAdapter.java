package dev.civicpulse.elections.adapter.out.persistence;

import dev.civicpulse.elections.application.port.out.ElectionCandidacyRepository;
import dev.civicpulse.elections.domain.model.ElectionCandidacy;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class ElectionCandidacyRepositoryAdapter implements ElectionCandidacyRepository {

  private final ElectionCandidacyJpaRepository jpaRepository;
  private final ElectionCandidacyMapper mapper;

  ElectionCandidacyRepositoryAdapter(ElectionCandidacyJpaRepository jpaRepository, ElectionCandidacyMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public ElectionCandidacy save(ElectionCandidacy candidacy) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(candidacy)));
  }

  @Override
  public List<ElectionCandidacy> findByElectionId(UUID electionId) {
    return jpaRepository.findByElectionId(electionId).stream().map(mapper::toDomain).toList();
  }

  @Override
  public boolean exists(UUID electionId, UUID politicianAccountId) {
    return jpaRepository.existsByElectionIdAndPoliticianAccountId(electionId, politicianAccountId);
  }
}
