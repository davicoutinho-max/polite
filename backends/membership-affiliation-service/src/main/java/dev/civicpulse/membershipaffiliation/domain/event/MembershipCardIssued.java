package dev.civicpulse.membershipaffiliation.domain.event;

import java.time.Instant;
import java.util.UUID;

public record MembershipCardIssued(UUID affiliationId, String memberNumber, Instant occurredAt) implements DomainEvent {

  @Override
  public String topic() {
    return "membership-card-issued";
  }
}
