package dev.civicpulse.membershipaffiliation.domain.event;

import java.time.Instant;
import java.util.UUID;

/** Consumed by Notification (fee-due reminders) and, indirectly, Payments (to create the
 * matching payment intent when the citizen chooses to pay). */
public record MembershipFeeGenerated(UUID feeId, UUID affiliationId, long amountCents, Instant occurredAt) implements DomainEvent {

  @Override
  public String topic() {
    return "membership-fee-generated";
  }
}
