package dev.civicpulse.payments.domain.event;

import java.time.Instant;
import java.util.UUID;

public record PaymentRefunded(UUID paymentIntentId, UUID referenceId, long amountCents, Instant occurredAt) implements DomainEvent {

  @Override
  public String topic() {
    return "payment-refunded";
  }
}
