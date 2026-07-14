package dev.civicpulse.membershipaffiliation.domain.exception;

import java.util.UUID;

public final class FeeAlreadyPaidException extends RuntimeException {

  public FeeAlreadyPaidException(UUID feeId) {
    super("Fee " + feeId + " is already paid");
  }
}
