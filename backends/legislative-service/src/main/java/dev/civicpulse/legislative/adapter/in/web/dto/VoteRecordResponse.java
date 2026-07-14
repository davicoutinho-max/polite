package dev.civicpulse.legislative.adapter.in.web.dto;

import dev.civicpulse.legislative.domain.model.VoteRecord;
import java.time.LocalDate;
import java.util.UUID;

public record VoteRecordResponse(UUID id, UUID legislativeItemId, String matter, LocalDate voteDate, String choice) {

  public static VoteRecordResponse from(VoteRecord voteRecord) {
    return new VoteRecordResponse(
        voteRecord.id().orElse(null),
        voteRecord.legislativeItemId().orElse(null),
        voteRecord.matter(),
        voteRecord.voteDate(),
        voteRecord.choice().code());
  }
}
