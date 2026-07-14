package dev.civicpulse.platformconfig.domain.exception;

public final class CannotRemoveDefaultLanguageException extends RuntimeException {

  public CannotRemoveDefaultLanguageException(String languageId) {
    super("Cannot remove '" + languageId + "' — it is the default language; set another language as default first");
  }
}
