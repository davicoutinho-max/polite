package dev.civicpulse.platformconfig.domain.exception;

public final class DuplicatePartyRegistrationException extends RuntimeException {

  public DuplicatePartyRegistrationException(String field) {
    super("A party with this " + field + " is already registered");
  }
}
