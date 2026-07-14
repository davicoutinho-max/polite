package dev.civicpulse.identity.domain.exception;

/** Deliberately generic — never reveals whether the email or the password was wrong. */
public final class InvalidCredentialsException extends RuntimeException {
  public InvalidCredentialsException() {
    super("Invalid email or password");
  }
}
