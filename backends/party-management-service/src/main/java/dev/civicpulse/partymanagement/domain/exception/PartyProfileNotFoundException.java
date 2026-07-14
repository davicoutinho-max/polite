package dev.civicpulse.partymanagement.domain.exception;

import java.util.UUID;

public final class PartyProfileNotFoundException extends RuntimeException {

  public PartyProfileNotFoundException(UUID partyId) {
    super("No profile found for party " + partyId);
  }
}
