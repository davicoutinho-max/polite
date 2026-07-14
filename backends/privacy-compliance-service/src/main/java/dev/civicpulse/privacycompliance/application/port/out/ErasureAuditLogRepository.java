package dev.civicpulse.privacycompliance.application.port.out;

import dev.civicpulse.privacycompliance.domain.model.ErasureAuditEntry;
import java.util.List;
import java.util.UUID;

public interface ErasureAuditLogRepository {

  ErasureAuditEntry save(ErasureAuditEntry entry);

  List<ErasureAuditEntry> findByDeletionRequestId(UUID deletionRequestId);

  boolean existsByDeletionRequestIdAndServiceName(UUID deletionRequestId, String serviceName);
}
