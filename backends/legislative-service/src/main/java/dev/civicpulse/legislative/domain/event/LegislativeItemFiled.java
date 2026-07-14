package dev.civicpulse.legislative.domain.event;

import java.time.Instant;
import java.util.UUID;

public record LegislativeItemFiled(
    UUID legislativeItemId, UUID politicianAccountId, String category, String reference, Instant occurredAt)
    implements DomainEvent {

  @Override
  public String topic() {
    return "legislative-item-filed";
  }
}
