package dev.civicpulse.platformconfig.domain.event;

import java.time.Instant;
import java.util.UUID;

/** Consumed by Directory Service — see its local PoliticianReassignedMessage (field names must
 * match exactly). */
public record PoliticianReassigned(UUID politicianAccountId, UUID partyId, Instant occurredAt) implements DomainEvent {

  @Override
  public String topic() {
    return "politician-reassigned";
  }
}
