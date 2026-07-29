package dev.civicpulse.participation.domain.exception;

/** Thrown when the real email send (Resend) can't be attempted or fails — either because no API
 * key is configured yet, or the provider itself rejected the request. Unlike the Gemini/Stripe
 * equivalents elsewhere in this system, a failure here must abort the signature attempt entirely
 * rather than degrading gracefully: without a delivered code, the citizen can never complete
 * verification, so continuing to CREATED with no way forward would be worse than a clear error. */
public class EmailDeliveryException extends RuntimeException {

  public EmailDeliveryException(String message) {
    super(message);
  }

  public EmailDeliveryException(String message, Throwable cause) {
    super(message, cause);
  }
}
