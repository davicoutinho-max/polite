package dev.civicpulse.notification.adapter.in.messaging.dto;

import java.time.Instant;
import java.util.UUID;

/** Local shape of fundraising-service's {@code ContributionReceived} event record — field names
 * must match the producer's record component names exactly. */
public record ContributionReceivedMessage(UUID fundraiserId, UUID contributionId, UUID supporterAccountId, long amountCents, Instant occurredAt) {}
