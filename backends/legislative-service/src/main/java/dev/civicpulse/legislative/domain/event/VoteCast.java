package dev.civicpulse.legislative.domain.event;

import java.time.Instant;
import java.util.UUID;

public record VoteCast(UUID voteRecordId, UUID politicianAccountId, String matter, String choice, Instant occurredAt)
    implements DomainEvent {

  @Override
  public String topic() {
    return "vote-cast";
  }
}
