package dev.civicpulse.notification.domain.exception;

public final class RecipientLookupException extends RuntimeException {

  public RecipientLookupException(String message, Throwable cause) {
    super(message, cause);
  }
}
