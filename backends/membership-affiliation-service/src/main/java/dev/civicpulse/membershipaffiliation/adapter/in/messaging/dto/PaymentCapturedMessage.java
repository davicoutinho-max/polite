package dev.civicpulse.membershipaffiliation.adapter.in.messaging.dto;

import java.time.Instant;
import java.util.UUID;

/** Local shape of payments-service's {@code PaymentCaptured} event record — {@code
 * referenceId} is the id of whatever this payment was for (here, a {@code membership_fees.id});
 * payments-service carries it through opaquely as metadata on the payment intent. Field names
 * must match the producer's record component names exactly once payments-service is built. */
public record PaymentCapturedMessage(UUID paymentIntentId, UUID referenceId, long amountCents, Instant occurredAt) {}
