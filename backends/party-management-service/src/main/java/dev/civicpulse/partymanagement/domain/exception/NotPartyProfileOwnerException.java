package dev.civicpulse.partymanagement.domain.exception;

import java.util.UUID;

/** Party profile reads are public, but writes are self-service only — a party editing its own
 * history/program/statute/cover, never another party's. Thrown when the gateway-validated
 * caller (X-Account-Id) doesn't match the party whose profile the path targets. */
public final class NotPartyProfileOwnerException extends RuntimeException {
  public NotPartyProfileOwnerException(UUID partyId) {
    super("Only " + partyId + " can edit this party's profile");
  }
}
