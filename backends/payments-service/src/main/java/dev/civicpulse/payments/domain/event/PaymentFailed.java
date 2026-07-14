package dev.civicpulse.payments.domain.event;

import java.time.Instant;
import java.util.UUID;

public record PaymentFailed(UUID paymentIntentId, UUID referenceId, String purpose, Instant occurredAt) implements DomainEvent {

  @Override
  public String topic() {
    return "payment-failed";
  }
}
