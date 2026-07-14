package dev.civicpulse.directory.domain.exception;

import java.util.UUID;

public final class PoliticianNotFoundException extends RuntimeException {

  public PoliticianNotFoundException(UUID accountId) {
    super("No politician projected for account " + accountId);
  }
}
