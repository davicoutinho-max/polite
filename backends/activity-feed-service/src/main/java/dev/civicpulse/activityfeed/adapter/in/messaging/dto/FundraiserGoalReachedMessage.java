package dev.civicpulse.activityfeed.adapter.in.messaging.dto;

import java.time.Instant;
import java.util.UUID;

public record FundraiserGoalReachedMessage(UUID fundraiserId, long raisedCents, Instant occurredAt) {}
