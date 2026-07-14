package dev.civicpulse.legislative.adapter.out.persistence;

import dev.civicpulse.legislative.application.port.out.VoteRecordRepository;
import dev.civicpulse.legislative.domain.model.VoteRecord;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class VoteRecordRepositoryAdapter implements VoteRecordRepository {

  private final VoteRecordJpaRepository jpaRepository;
  private final VoteRecordMapper mapper;

  VoteRecordRepositoryAdapter(VoteRecordJpaRepository jpaRepository, VoteRecordMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public VoteRecord save(VoteRecord voteRecord) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(voteRecord)));
  }

  @Override
  public List<VoteRecord> findByPolitician(UUID politicianAccountId) {
    return jpaRepository.findByPoliticianAccountIdOrderByVoteDateDesc(politicianAccountId).stream().map(mapper::toDomain).toList();
  }
}
