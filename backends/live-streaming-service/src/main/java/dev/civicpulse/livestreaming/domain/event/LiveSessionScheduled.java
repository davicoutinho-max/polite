package dev.civicpulse.livestreaming.domain.event;

import java.time.Instant;
import java.util.UUID;

public record LiveSessionScheduled(UUID sessionId, UUID hostAccountId, Instant scheduledFor, Instant occurredAt) implements DomainEvent {

  @Override
  public String topic() {
    return "live-session-scheduled";
  }
}
