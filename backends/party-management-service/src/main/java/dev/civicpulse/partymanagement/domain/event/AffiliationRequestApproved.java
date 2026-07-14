package dev.civicpulse.partymanagement.domain.event;

import java.time.Instant;
import java.util.UUID;

/** Consumed by Membership &amp; Affiliation to advance its own saga state and by Notification. */
public record AffiliationRequestApproved(UUID requestId, UUID partyId, UUID citizenAccountId, Instant occurredAt)
    implements DomainEvent {

  @Override
  public String topic() {
    return "affiliation-request-approved";
  }
}
