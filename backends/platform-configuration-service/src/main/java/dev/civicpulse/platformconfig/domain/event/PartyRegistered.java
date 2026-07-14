package dev.civicpulse.platformconfig.domain.event;

import java.time.Instant;
import java.util.UUID;

/** Consumed by Directory Service (public party catalog) and Party Management (creates the
 * blank editable profile). Field names must match directory-service's/party-management's local
 * PartyRegisteredMessage exactly — see directory-service's RepresentativeLinkedMessage javadoc
 * for why a mismatch silently deserializes to null instead of failing loudly. */
public record PartyRegistered(UUID partyId, String name, String acronym, int number, String president, String ideology, Instant occurredAt)
    implements DomainEvent {

  @Override
  public String topic() {
    return "party-registered";
  }
}
