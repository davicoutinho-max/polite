package dev.civicpulse.legislative.domain.event;

import java.time.Instant;
import java.util.UUID;

public record LegislativeItemStatusChanged(UUID legislativeItemId, UUID politicianAccountId, String status, Instant occurredAt)
    implements DomainEvent {

  @Override
  public String topic() {
    return "legislative-item-status-changed";
  }
}
