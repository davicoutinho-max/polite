package dev.civicpulse.platformconfig.domain.model;

import java.util.Objects;
import java.util.UUID;

public final class TranslationKey {

  private final UUID id;
  private final String key;

  private TranslationKey(UUID id, String key) {
    this.id = Objects.requireNonNull(id);
    this.key = requireNonBlank(key);
  }

  public static TranslationKey create(UUID id, String key) {
    return new TranslationKey(id, key);
  }

  public static TranslationKey reconstitute(UUID id, String key) {
    return new TranslationKey(id, key);
  }

  private static String requireNonBlank(String key) {
    if (key == null || key.isBlank()) {
      throw new IllegalArgumentException("key must not be blank");
    }
    return key;
  }

  public UUID id() {
    return id;
  }

  public String key() {
    return key;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof TranslationKey other)) return false;
    return id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
