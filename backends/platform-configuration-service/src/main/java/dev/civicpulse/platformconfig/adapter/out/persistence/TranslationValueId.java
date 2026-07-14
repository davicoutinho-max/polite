package dev.civicpulse.platformconfig.adapter.out.persistence;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class TranslationValueId implements Serializable {

  private UUID translationKeyId;
  private String languageId;

  public TranslationValueId() {}

  public TranslationValueId(UUID translationKeyId, String languageId) {
    this.translationKeyId = translationKeyId;
    this.languageId = languageId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof TranslationValueId other)) return false;
    return Objects.equals(translationKeyId, other.translationKeyId) && Objects.equals(languageId, other.languageId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(translationKeyId, languageId);
  }
}
