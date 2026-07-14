package dev.civicpulse.notification.adapter.in.messaging.dto;

import java.time.Instant;
import java.util.UUID;

/** Local shape of membership-affiliation-service's {@code AffiliationConfirmed} event record —
 * field names must match the producer's record component names exactly. */
public record AffiliationConfirmedMessage(UUID affiliationId, UUID citizenAccountId, UUID partyId, Instant occurredAt) {}
