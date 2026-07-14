package dev.civicpulse.membershipaffiliation.domain.exception;

import java.util.UUID;

public final class MembershipFeeNotFoundException extends RuntimeException {

  public MembershipFeeNotFoundException(UUID id) {
    super("No membership fee found with id " + id);
  }
}
