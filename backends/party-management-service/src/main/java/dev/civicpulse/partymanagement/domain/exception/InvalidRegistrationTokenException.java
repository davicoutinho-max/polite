package dev.civicpulse.partymanagement.domain.exception;

public final class InvalidRegistrationTokenException extends RuntimeException {

  public InvalidRegistrationTokenException() {
    super("This invite link is invalid, expired, or has already been used");
  }
}
