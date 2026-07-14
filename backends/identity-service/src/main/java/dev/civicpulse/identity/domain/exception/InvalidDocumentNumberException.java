package dev.civicpulse.identity.domain.exception;

import dev.civicpulse.identity.domain.model.DocumentType;

/** Mirrors the frontend's br-documents.ts validation exactly: a digit-count check, not a full
 * checksum — documented as an MVP-scope decision, not an oversight. */
public final class InvalidDocumentNumberException extends RuntimeException {
  public InvalidDocumentNumberException(DocumentType type) {
    super("Enter a valid " + type.code().toUpperCase() + " (" + type.digitCount() + " digits)");
  }
}
