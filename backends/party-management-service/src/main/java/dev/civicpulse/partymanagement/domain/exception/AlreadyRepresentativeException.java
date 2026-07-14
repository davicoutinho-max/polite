package dev.civicpulse.partymanagement.domain.exception;

public final class AlreadyRepresentativeException extends RuntimeException {

  public AlreadyRepresentativeException() {
    super("This politician is already linked to this party");
  }
}
