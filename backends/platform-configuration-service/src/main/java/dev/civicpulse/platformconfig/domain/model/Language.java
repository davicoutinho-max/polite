package dev.civicpulse.platformconfig.domain.model;

import java.util.Objects;

/** {@code id} is the BCP-47 tag itself (e.g. {@code "pt-br"}), not a surrogate UUID — see
 * schema.sql's comment on {@code languages.id}. Exactly one language may have {@code
 * isDefault = true} at a time (enforced by a partial unique index); see LanguageService for how
 * that invariant is upheld across two rows. */
public final class Language {

  private final String id;
  private final String name;
  private final String code;
  private final boolean isDefault;

  private Language(String id, String name, String code, boolean isDefault) {
    this.id = requireNonBlank(id, "id");
    this.name = requireNonBlank(name, "name");
    this.code = requireNonBlank(code, "code");
    this.isDefault = isDefault;
  }

  public static Language create(String id, String name, String code, boolean isDefault) {
    return new Language(id, name, code, isDefault);
  }

  public static Language reconstitute(String id, String name, String code, boolean isDefault) {
    return new Language(id, name, code, isDefault);
  }

  private static String requireNonBlank(String value, String field) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(field + " must not be blank");
    }
    return value;
  }

  public String id() {
    return id;
  }

  public String name() {
    return name;
  }

  public String code() {
    return code;
  }

  public boolean isDefault() {
    return isDefault;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Language other)) return false;
    return id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
