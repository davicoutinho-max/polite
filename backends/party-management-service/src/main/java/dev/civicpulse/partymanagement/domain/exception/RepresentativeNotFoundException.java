package dev.civicpulse.partymanagement.domain.exception;

import java.util.UUID;

public final class RepresentativeNotFoundException extends RuntimeException {

  public RepresentativeNotFoundException(UUID partyId, UUID politicianAccountId) {
    super("No representative link found for party " + partyId + " and politician " + politicianAccountId);
  }
}
