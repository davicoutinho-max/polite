package dev.civicpulse.messaging.domain.exception;

public final class NotAParticipantException extends RuntimeException {

  public NotAParticipantException() {
    super("This account is not a participant of this conversation");
  }
}
