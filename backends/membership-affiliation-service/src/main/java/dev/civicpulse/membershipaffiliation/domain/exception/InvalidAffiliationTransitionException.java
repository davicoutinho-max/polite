package dev.civicpulse.membershipaffiliation.domain.exception;

import dev.civicpulse.membershipaffiliation.domain.model.AffiliationStatus;

public final class InvalidAffiliationTransitionException extends RuntimeException {

  public InvalidAffiliationTransitionException(AffiliationStatus from, AffiliationStatus to) {
    super("Cannot transition affiliation from " + from.code() + " to " + to.code());
  }
}
