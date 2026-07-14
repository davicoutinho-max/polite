package dev.civicpulse.payments.adapter.in.web.dto;

import dev.civicpulse.payments.domain.model.PaymentIntent;
import java.time.Instant;
import java.util.UUID;

public record PaymentIntentResponse(
    UUID id,
    String purpose,
    UUID referenceId,
    UUID payerAccountId,
    UUID payeeId,
    long amountCents,
    String currency,
    String status,
    String gateway,
    String gatewayRef,
    Instant createdAt,
    Instant updatedAt) {

  public static PaymentIntentResponse from(PaymentIntent intent) {
    return new PaymentIntentResponse(
        intent.id(),
        intent.purpose().code(),
        intent.referenceId(),
        intent.payerAccountId(),
        intent.payeeId(),
        intent.amountCents(),
        intent.currency(),
        intent.status().code(),
        intent.gateway().code(),
        intent.gatewayRef().orElse(null),
        intent.createdAt(),
        intent.updatedAt());
  }
}
