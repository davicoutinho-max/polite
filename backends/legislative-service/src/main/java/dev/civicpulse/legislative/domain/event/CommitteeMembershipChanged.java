package dev.civicpulse.legislative.domain.event;

import java.time.Instant;
import java.util.UUID;

public record CommitteeMembershipChanged(UUID committeeMembershipId, UUID politicianAccountId, String name, Instant occurredAt)
    implements DomainEvent {

  @Override
  public String topic() {
    return "committee-membership-changed";
  }
}
