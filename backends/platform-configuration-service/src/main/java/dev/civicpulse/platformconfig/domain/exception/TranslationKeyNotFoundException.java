package dev.civicpulse.platformconfig.domain.exception;

public final class TranslationKeyNotFoundException extends RuntimeException {

  public TranslationKeyNotFoundException(String key) {
    super("No translation key found: " + key);
  }
}
