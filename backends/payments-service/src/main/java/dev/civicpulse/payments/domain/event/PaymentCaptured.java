package dev.civicpulse.payments.domain.event;

import java.time.Instant;
import java.util.UUID;

/** Consumed by Membership &amp; Affiliation (marks a fee paid) and Fundraising (records a
 * contribution) — see membership-affiliation-service's local PaymentCapturedMessage (field
 * names must match exactly; {@code referenceId} is the id of whatever this payment was for,
 * opaque to this service). */
public record PaymentCaptured(UUID paymentIntentId, UUID referenceId, long amountCents, Instant occurredAt) implements DomainEvent {

  @Override
  public String topic() {
    return "payment-captured";
  }
}
