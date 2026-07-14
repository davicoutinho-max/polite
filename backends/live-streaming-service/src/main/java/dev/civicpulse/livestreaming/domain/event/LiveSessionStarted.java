package dev.civicpulse.livestreaming.domain.event;

import java.time.Instant;
import java.util.UUID;

public record LiveSessionStarted(UUID sessionId, UUID hostAccountId, Instant occurredAt) implements DomainEvent {

  @Override
  public String topic() {
    return "live-session-started";
  }
}
