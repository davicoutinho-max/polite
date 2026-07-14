package dev.civicpulse.fundraising.domain.event;

import java.time.Instant;
import java.util.UUID;

public record ContributionReceived(UUID fundraiserId, UUID contributionId, UUID supporterAccountId, long amountCents, Instant occurredAt)
    implements DomainEvent {

  @Override
  public String topic() {
    return "contribution-received";
  }
}
