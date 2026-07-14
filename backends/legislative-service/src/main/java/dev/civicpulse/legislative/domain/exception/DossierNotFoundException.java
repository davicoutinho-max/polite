package dev.civicpulse.legislative.domain.exception;

import java.util.UUID;

public final class DossierNotFoundException extends RuntimeException {

  public DossierNotFoundException(UUID politicianAccountId) {
    super("No politician dossier extension found for account " + politicianAccountId);
  }
}
