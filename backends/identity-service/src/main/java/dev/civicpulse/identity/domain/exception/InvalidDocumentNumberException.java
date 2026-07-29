package dev.civicpulse.identity.domain.exception;

import dev.civicpulse.identity.domain.model.DocumentType;

/** Mirrors the frontend's br-documents.ts check-digit validation exactly (see
 * DocumentNumberValidator) — enforced here too since a direct API caller never goes through the
 * frontend's own check. */
public final class InvalidDocumentNumberException extends RuntimeException {
  public InvalidDocumentNumberException(DocumentType type) {
    super("Enter a valid " + type.code().toUpperCase() + " (" + type.digitCount() + " digits, with valid check digits)");
  }
}
