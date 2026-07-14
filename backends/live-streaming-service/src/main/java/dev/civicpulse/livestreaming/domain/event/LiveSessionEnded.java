package dev.civicpulse.livestreaming.domain.event;

import java.time.Instant;
import java.util.UUID;

public record LiveSessionEnded(UUID sessionId, UUID hostAccountId, int peakViewers, Instant occurredAt) implements DomainEvent {

  @Override
  public String topic() {
    return "live-session-ended";
  }
}
