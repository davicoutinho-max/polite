package dev.civicpulse.legislative.domain.exception;

import java.util.UUID;

/** Dossier reads are public (anyone can view a politician's declared education/profession/etc.),
 * but writes are self-service only — a politician editing their own dossier, never anyone else's.
 * Thrown when the gateway-validated caller (X-Account-Id) doesn't match the politician whose
 * dossier the path targets. */
public final class NotDossierOwnerException extends RuntimeException {
  public NotDossierOwnerException(UUID politicianAccountId) {
    super("Only " + politicianAccountId + " can edit this dossier");
  }
}
