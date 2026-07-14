package dev.civicpulse.notification.adapter.in.messaging.dto;

import java.time.Instant;
import java.util.UUID;

/** Local shape of fundraising-service's {@code FundraiserGoalReached} event record — field names
 * must match the producer's record component names exactly. */
public record FundraiserGoalReachedMessage(UUID fundraiserId, long raisedCents, Instant occurredAt) {}
