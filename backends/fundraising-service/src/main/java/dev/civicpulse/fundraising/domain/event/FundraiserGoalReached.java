package dev.civicpulse.fundraising.domain.event;

import java.time.Instant;
import java.util.UUID;

public record FundraiserGoalReached(UUID fundraiserId, long raisedCents, Instant occurredAt) implements DomainEvent {

  @Override
  public String topic() {
    return "fundraiser-goal-reached";
  }
}
