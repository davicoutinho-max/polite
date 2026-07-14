package dev.civicpulse.payments.adapter.in.web.dto;

import dev.civicpulse.payments.domain.model.PaymentMethod;
import java.time.Instant;
import java.util.UUID;

public record PaymentMethodResponse(UUID id, UUID accountId, String type, String tokenRef, Instant createdAt) {

  public static PaymentMethodResponse from(PaymentMethod method) {
    return new PaymentMethodResponse(method.id(), method.accountId(), method.type().code(), method.tokenRef(), method.createdAt());
  }
}
