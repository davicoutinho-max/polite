package dev.civicpulse.privacycompliance.adapter.out.persistence;

import dev.civicpulse.privacycompliance.domain.model.DataExportRequest;
import org.springframework.stereotype.Component;

@Component
class DataExportRequestMapper {

  DataExportRequest toDomain(DataExportRequestJpaEntity entity) {
    return DataExportRequest.reconstitute(
        entity.getId(), entity.getAccountId(), entity.getStatus(), entity.getRequestedAt(), entity.getCompletedAt(), entity.getDownloadUrl(),
        entity.getExpiresAt());
  }

  DataExportRequestJpaEntity toEntity(DataExportRequest request) {
    return new DataExportRequestJpaEntity(
        request.id(),
        request.accountId(),
        request.status(),
        request.requestedAt(),
        request.completedAt().orElse(null),
        request.downloadUrl().orElse(null),
        request.expiresAt().orElse(null));
  }
}
