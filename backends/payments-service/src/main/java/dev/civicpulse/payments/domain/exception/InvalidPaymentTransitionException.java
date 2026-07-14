package dev.civicpulse.payments.domain.exception;

import dev.civicpulse.payments.domain.model.PaymentStatus;

public final class InvalidPaymentTransitionException extends RuntimeException {

  public InvalidPaymentTransitionException(PaymentStatus from, PaymentStatus to) {
    super("Cannot transition payment intent from " + from.code() + " to " + to.code());
  }
}
