package dev.civicpulse.privacycompliance.domain.exception;

import java.util.UUID;

public final class AccountDeletionRequestNotFoundException extends RuntimeException {

  public AccountDeletionRequestNotFoundException(UUID id) {
    super("No account deletion request found with id " + id);
  }
}
