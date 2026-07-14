package dev.civicpulse.partymanagement.domain.exception;

import java.util.UUID;

public final class PartyMemberNotFoundException extends RuntimeException {

  public PartyMemberNotFoundException(UUID partyId, UUID citizenAccountId) {
    super("No party member found for party " + partyId + " and citizen " + citizenAccountId);
  }
}
