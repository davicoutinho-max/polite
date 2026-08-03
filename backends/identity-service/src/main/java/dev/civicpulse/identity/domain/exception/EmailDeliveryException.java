package dev.civicpulse.identity.domain.exception;

/** Thrown when the real email send (Resend) can't be attempted or fails — see
 * participation-service's identically-named exception for the same reasoning: without a
 * delivered invite link, the recipient has no way to redeem the token, so the issue attempt
 * must abort rather than silently succeed with nothing ever sent. */
public class EmailDeliveryException extends RuntimeException {

  public EmailDeliveryException(String message) {
    super(message);
  }

  public EmailDeliveryException(String message, Throwable cause) {
    super(message, cause);
  }
}
