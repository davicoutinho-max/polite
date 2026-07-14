package dev.civicpulse.identity.domain.event;

import java.time.Instant;
import java.util.UUID;

public record AccountVerified(UUID accountId, Instant occurredAt) implements DomainEvent {
  @Override
  public String topic() {
    return "account-verified";
  }
}
