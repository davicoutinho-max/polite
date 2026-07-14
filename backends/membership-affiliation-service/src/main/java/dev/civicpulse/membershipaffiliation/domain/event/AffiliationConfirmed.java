package dev.civicpulse.membershipaffiliation.domain.event;

import java.time.Instant;
import java.util.UUID;

/** Consumed by Party Management (to increment member_count) and Notification. */
public record AffiliationConfirmed(UUID affiliationId, UUID citizenAccountId, UUID partyId, Instant occurredAt) implements DomainEvent {

  @Override
  public String topic() {
    return "affiliation-confirmed";
  }
}
