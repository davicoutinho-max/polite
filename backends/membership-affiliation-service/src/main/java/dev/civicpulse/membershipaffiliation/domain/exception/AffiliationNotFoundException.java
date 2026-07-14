package dev.civicpulse.membershipaffiliation.domain.exception;

import java.util.UUID;

public final class AffiliationNotFoundException extends RuntimeException {

  public AffiliationNotFoundException(UUID id) {
    super("No affiliation found with id " + id);
  }
}
