package dev.civicpulse.fundraising.domain.exception;

import java.util.UUID;

public final class LedgerNotPublicException extends RuntimeException {

  public LedgerNotPublicException(UUID fundraiserId) {
    super("Fundraiser " + fundraiserId + " has not made its contribution ledger public");
  }
}
