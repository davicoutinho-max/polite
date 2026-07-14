package dev.civicpulse.partymanagement.domain.event;

import java.time.Instant;
import java.util.UUID;

public record PartyProfileUpdated(UUID partyId, Instant occurredAt) implements DomainEvent {

  @Override
  public String topic() {
    return "party-profile-updated";
  }
}
