package dev.civicpulse.partymanagement.domain.event;

import java.time.Instant;
import java.util.UUID;

/** Consumed by Directory Service to enrich its politician projection with the party linkage
 * (see docs/db/directory-service/schema.sql). */
public record PoliticianRegistered(UUID politicianAccountId, UUID partyId, String cpfHash, String cnpjHash, Instant occurredAt)
    implements DomainEvent {

  @Override
  public String topic() {
    return "politician-registered";
  }
}
