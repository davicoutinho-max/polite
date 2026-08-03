package dev.civicpulse.identity.domain.exception;

public final class InvalidRegistrationTokenException extends RuntimeException {

  public InvalidRegistrationTokenException() {
    super("This invite link is invalid, expired, or has already been used");
  }
}
