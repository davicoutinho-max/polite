package dev.civicpulse.membershipaffiliation.domain.event;

import java.time.Instant;
import java.util.UUID;

public record AffiliationUnderReview(UUID affiliationId, Instant occurredAt) implements DomainEvent {

  @Override
  public String topic() {
    return "affiliation-under-review";
  }
}
