package dev.civicpulse.fundraising.domain.event;

import java.time.Instant;

public sealed interface DomainEvent permits FundraiserCreated, ContributionReceived, FundraiserGoalReached {

  String topic();

  Instant occurredAt();
}
