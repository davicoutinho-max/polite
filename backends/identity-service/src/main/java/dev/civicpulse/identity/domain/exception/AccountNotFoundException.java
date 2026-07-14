package dev.civicpulse.identity.domain.exception;

public final class AccountNotFoundException extends RuntimeException {
  public AccountNotFoundException(String identifier) {
    super("No account found for " + identifier);
  }
}
