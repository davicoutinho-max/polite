package dev.civicpulse.elections.domain.exception;

public final class PoliticianLookupException extends RuntimeException {

  public PoliticianLookupException(String message, Throwable cause) {
    super(message, cause);
  }
}
