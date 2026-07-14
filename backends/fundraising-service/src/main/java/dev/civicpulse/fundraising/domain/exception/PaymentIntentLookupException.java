package dev.civicpulse.fundraising.domain.exception;

public final class PaymentIntentLookupException extends RuntimeException {

  public PaymentIntentLookupException(String message, Throwable cause) {
    super(message, cause);
  }
}
