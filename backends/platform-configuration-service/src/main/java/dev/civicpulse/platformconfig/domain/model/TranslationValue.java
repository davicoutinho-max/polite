package dev.civicpulse.platformconfig.domain.model;

import java.util.Objects;
import java.util.UUID;

public final class TranslationValue {

  private final UUID translationKeyId;
  private final String languageId;
  private String value;

  private TranslationValue(UUID translationKeyId, String languageId, String value) {
    this.translationKeyId = Objects.requireNonNull(translationKeyId);
    this.languageId = Objects.requireNonNull(languageId);
    this.value = requireNonBlank(value);
  }

  public static TranslationValue set(UUID translationKeyId, String languageId, String value) {
    return new TranslationValue(translationKeyId, languageId, value);
  }

  public static TranslationValue reconstitute(UUID translationKeyId, String languageId, String value) {
    return new TranslationValue(translationKeyId, languageId, value);
  }

  public void update(String newValue) {
    this.value = requireNonBlank(newValue);
  }

  private static String requireNonBlank(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("value must not be blank");
    }
    return value;
  }

  public UUID translationKeyId() {
    return translationKeyId;
  }

  public String languageId() {
    return languageId;
  }

  public String value() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof TranslationValue other)) return false;
    return translationKeyId.equals(other.translationKeyId) && languageId.equals(other.languageId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(translationKeyId, languageId);
  }
}
