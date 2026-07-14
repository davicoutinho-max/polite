package dev.civicpulse.membershipaffiliation.domain.exception;

public final class ActiveAffiliationAlreadyExistsException extends RuntimeException {

  public ActiveAffiliationAlreadyExistsException() {
    super("This citizen already has an active (non-rejected) affiliation with this party");
  }
}
