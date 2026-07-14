package dev.civicpulse.privacycompliance.adapter.in.web.dto;

import dev.civicpulse.privacycompliance.domain.model.DataExportRequest;
import java.time.Instant;
import java.util.UUID;

public record DataExportRequestResponse(
    UUID id, UUID accountId, String status, Instant requestedAt, Instant completedAt, String downloadUrl, Instant expiresAt) {

  public static DataExportRequestResponse from(DataExportRequest request) {
    return new DataExportRequestResponse(
        request.id(),
        request.accountId(),
        request.status().code(),
        request.requestedAt(),
        request.completedAt().orElse(null),
        request.downloadUrl().orElse(null),
        request.expiresAt().orElse(null));
  }
}
