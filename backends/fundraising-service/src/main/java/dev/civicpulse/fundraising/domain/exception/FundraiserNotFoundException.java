package dev.civicpulse.fundraising.domain.exception;

import java.util.UUID;

public final class FundraiserNotFoundException extends RuntimeException {

  public FundraiserNotFoundException(UUID id) {
    super("No fundraiser found with id " + id);
  }
}
