package dev.civicpulse.privacycompliance.domain.exception;

import dev.civicpulse.privacycompliance.domain.model.DeletionStatus;

public final class InvalidDeletionTransitionException extends RuntimeException {

  public InvalidDeletionTransitionException(DeletionStatus from, DeletionStatus to) {
    super("Cannot transition account deletion request from " + from.code() + " to " + to.code());
  }
}
