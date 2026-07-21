package dev.civicpulse.messaging.domain.exception;

public final class NotMessageSenderException extends RuntimeException {

  public NotMessageSenderException() {
    super("Only the original sender can edit or delete this message");
  }
}
