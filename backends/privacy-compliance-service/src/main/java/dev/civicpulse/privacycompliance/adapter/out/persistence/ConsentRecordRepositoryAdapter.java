package dev.civicpulse.privacycompliance.adapter.out.persistence;

import dev.civicpulse.privacycompliance.application.port.out.ConsentRecordRepository;
import dev.civicpulse.privacycompliance.domain.model.ConsentPurpose;
import dev.civicpulse.privacycompliance.domain.model.ConsentRecord;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class ConsentRecordRepositoryAdapter implements ConsentRecordRepository {

  private final ConsentRecordJpaRepository jpaRepository;
  private final ConsentRecordMapper mapper;

  ConsentRecordRepositoryAdapter(ConsentRecordJpaRepository jpaRepository, ConsentRecordMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public ConsentRecord save(ConsentRecord consentRecord) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(consentRecord)));
  }

  @Override
  public Optional<ConsentRecord> findByAccountAndPurpose(UUID accountId, ConsentPurpose purpose) {
    return jpaRepository.findByAccountIdAndPurpose(accountId, purpose.code()).map(mapper::toDomain);
  }

  @Override
  public List<ConsentRecord> findByAccount(UUID accountId) {
    return jpaRepository.findByAccountId(accountId).stream().map(mapper::toDomain).toList();
  }
}
