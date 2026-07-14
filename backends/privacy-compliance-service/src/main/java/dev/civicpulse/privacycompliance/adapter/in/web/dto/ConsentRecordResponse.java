package dev.civicpulse.privacycompliance.adapter.in.web.dto;

import dev.civicpulse.privacycompliance.domain.model.ConsentRecord;
import java.time.Instant;
import java.util.UUID;

public record ConsentRecordResponse(UUID accountId, String purpose, boolean granted, Instant updatedAt) {

  public static ConsentRecordResponse from(ConsentRecord record) {
    return new ConsentRecordResponse(record.accountId(), record.purpose().code(), record.granted(), record.updatedAt());
  }
}
