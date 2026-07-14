package dev.civicpulse.partymanagement.domain.exception;

import java.util.UUID;

public final class AffiliationRequestNotFoundException extends RuntimeException {

  public AffiliationRequestNotFoundException(UUID id) {
    super("No affiliation request found with id " + id);
  }
}
