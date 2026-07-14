package dev.civicpulse.privacycompliance.domain.event;

import java.time.Instant;
import java.util.UUID;

public record ConsentUpdated(UUID accountId, String purpose, boolean granted, Instant occurredAt) implements DomainEvent {

  @Override
  public String topic() {
    return "consent-updated";
  }
}
