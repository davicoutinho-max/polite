package dev.civicpulse.membershipaffiliation.domain.event;

import java.time.Instant;
import java.util.UUID;

/** Consumed by Party Management to create its review row — see its local
 * AffiliationRequestedMessage (field names must match exactly). */
public record AffiliationRequested(UUID affiliationId, UUID partyId, UUID citizenAccountId, String city, Instant occurredAt)
    implements DomainEvent {

  @Override
  public String topic() {
    return "affiliation-requested";
  }
}
