package dev.civicpulse.identity.domain.exception;

/** Thrown when a registration attempt collides on email, handle, or document number hash —
 * all three carry a UNIQUE constraint in identity-service/schema.sql. */
public final class DuplicateAccountException extends RuntimeException {

  private final String field;

  public DuplicateAccountException(String field) {
    super("An account with this " + field + " already exists");
    this.field = field;
  }

  public String field() {
    return field;
  }
}
