package dev.civicpulse.partymanagement.domain.event;

import java.time.Instant;
import java.util.UUID;

public record RepresentativeRemoved(UUID partyId, UUID politicianAccountId, Instant occurredAt) implements DomainEvent {

  @Override
  public String topic() {
    return "representative-removed";
  }
}
