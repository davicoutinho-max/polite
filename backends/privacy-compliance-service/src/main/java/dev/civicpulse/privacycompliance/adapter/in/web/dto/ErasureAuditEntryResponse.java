package dev.civicpulse.privacycompliance.adapter.in.web.dto;

import dev.civicpulse.privacycompliance.domain.model.ErasureAuditEntry;
import java.time.Instant;
import java.util.UUID;

public record ErasureAuditEntryResponse(Long id, UUID deletionRequestId, String serviceName, Instant erasedAt, Integer recordCount) {

  public static ErasureAuditEntryResponse from(ErasureAuditEntry entry) {
    return new ErasureAuditEntryResponse(
        entry.id().orElse(null), entry.deletionRequestId(), entry.serviceName(), entry.erasedAt(), entry.recordCount().orElse(null));
  }
}
