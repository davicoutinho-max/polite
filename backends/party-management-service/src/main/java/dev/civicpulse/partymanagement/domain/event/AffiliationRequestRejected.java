package dev.civicpulse.partymanagement.domain.event;

import java.time.Instant;
import java.util.UUID;

public record AffiliationRequestRejected(UUID requestId, UUID partyId, UUID citizenAccountId, Instant occurredAt)
    implements DomainEvent {

  @Override
  public String topic() {
    return "affiliation-request-rejected";
  }
}
