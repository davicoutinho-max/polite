package dev.civicpulse.privacycompliance.adapter.out.persistence;

import dev.civicpulse.privacycompliance.domain.model.ErasureAuditEntry;
import org.springframework.stereotype.Component;

@Component
class ErasureAuditLogMapper {

  ErasureAuditEntry toDomain(ErasureAuditLogJpaEntity entity) {
    return ErasureAuditEntry.reconstitute(
        entity.getId(), entity.getDeletionRequestId(), entity.getServiceName(), entity.getErasedAt(), entity.getRecordCount());
  }

  ErasureAuditLogJpaEntity toEntity(ErasureAuditEntry entry) {
    return new ErasureAuditLogJpaEntity(
        entry.id().orElse(null), entry.deletionRequestId(), entry.serviceName(), entry.erasedAt(), entry.recordCount().orElse(null));
  }
}
