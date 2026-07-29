package dev.civicpulse.membershipaffiliation.adapter.in.web.dto;

import dev.civicpulse.membershipaffiliation.domain.model.AffiliationStatusHistoryEntry;
import java.time.Instant;
import java.util.UUID;

public record AffiliationStatusHistoryResponse(UUID id, String fromStatus, String toStatus, String changedBy, Instant changedAt) {

  public static AffiliationStatusHistoryResponse from(AffiliationStatusHistoryEntry entry) {
    return new AffiliationStatusHistoryResponse(
        entry.id(),
        entry.fromStatus().map(s -> s.code()).orElse(null),
        entry.toStatus().code(),
        entry.changedBy().code(),
        entry.changedAt());
  }
}
