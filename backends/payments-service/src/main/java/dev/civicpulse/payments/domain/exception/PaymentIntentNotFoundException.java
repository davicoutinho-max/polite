package dev.civicpulse.payments.domain.exception;

import java.util.UUID;

public final class PaymentIntentNotFoundException extends RuntimeException {

  public PaymentIntentNotFoundException(UUID id) {
    super("No payment intent found with id " + id);
  }
}
