package dev.civicpulse.platformconfig.domain.exception;

import java.util.UUID;

public final class PoliticianAssignmentNotFoundException extends RuntimeException {

  public PoliticianAssignmentNotFoundException(UUID politicianAccountId) {
    super("No party assignment found for politician " + politicianAccountId);
  }
}
