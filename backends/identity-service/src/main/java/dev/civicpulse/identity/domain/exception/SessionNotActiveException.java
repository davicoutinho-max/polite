package dev.civicpulse.identity.domain.exception;

public final class SessionNotActiveException extends RuntimeException {
  public SessionNotActiveException() {
    super("Session is expired or has been revoked");
  }
}
