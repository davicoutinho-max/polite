package dev.civicpulse.identity.domain.event;

import java.time.Instant;
import java.util.UUID;

public record SessionRevoked(UUID sessionId, Instant occurredAt) implements DomainEvent {
  @Override
  public String topic() {
    return "session-revoked";
  }
}
