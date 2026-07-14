package dev.civicpulse.platformconfig.domain.exception;

public final class LanguageNotFoundException extends RuntimeException {

  public LanguageNotFoundException(String id) {
    super("No language found with id " + id);
  }
}
