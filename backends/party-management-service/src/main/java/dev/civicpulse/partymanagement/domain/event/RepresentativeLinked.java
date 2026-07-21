package dev.civicpulse.partymanagement.domain.event;

import java.time.Instant;
import java.util.UUID;

/** Consumed by Directory Service to project office/role/state onto a politician (see
 * docs/db/directory-service/schema.sql). */
public record RepresentativeLinked(UUID partyId, UUID politicianAccountId, String roleTitle, String state, Instant occurredAt)
    implements DomainEvent {

  @Override
  public String topic() {
    return "representative-linked";
  }
}
