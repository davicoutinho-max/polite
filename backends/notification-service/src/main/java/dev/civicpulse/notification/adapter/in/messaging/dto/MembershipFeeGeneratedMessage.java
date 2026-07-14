package dev.civicpulse.notification.adapter.in.messaging.dto;

import java.time.Instant;
import java.util.UUID;

/** Local shape of membership-affiliation-service's {@code MembershipFeeGenerated} event record —
 * field names must match the producer's record component names exactly. */
public record MembershipFeeGeneratedMessage(UUID feeId, UUID affiliationId, long amountCents, Instant occurredAt) {}
