package dev.civicpulse.fundraising.domain.event;

import java.time.Instant;
import java.util.UUID;

public record FundraiserCreated(UUID fundraiserId, UUID organizerAccountId, String category, long goalCents, Instant occurredAt)
    implements DomainEvent {

  @Override
  public String topic() {
    return "fundraiser-created";
  }
}
