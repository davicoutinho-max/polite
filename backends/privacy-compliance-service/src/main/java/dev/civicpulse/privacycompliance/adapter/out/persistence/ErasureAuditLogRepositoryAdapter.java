package dev.civicpulse.privacycompliance.adapter.out.persistence;

import dev.civicpulse.privacycompliance.application.port.out.ErasureAuditLogRepository;
import dev.civicpulse.privacycompliance.domain.model.ErasureAuditEntry;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class ErasureAuditLogRepositoryAdapter implements ErasureAuditLogRepository {

  private final ErasureAuditLogJpaRepository jpaRepository;
  private final ErasureAuditLogMapper mapper;

  ErasureAuditLogRepositoryAdapter(ErasureAuditLogJpaRepository jpaRepository, ErasureAuditLogMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public ErasureAuditEntry save(ErasureAuditEntry entry) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(entry)));
  }

  @Override
  public List<ErasureAuditEntry> findByDeletionRequestId(UUID deletionRequestId) {
    return jpaRepository.findByDeletionRequestId(deletionRequestId).stream().map(mapper::toDomain).toList();
  }

  @Override
  public boolean existsByDeletionRequestIdAndServiceName(UUID deletionRequestId, String serviceName) {
    return jpaRepository.existsByDeletionRequestIdAndServiceName(deletionRequestId, serviceName);
  }
}
