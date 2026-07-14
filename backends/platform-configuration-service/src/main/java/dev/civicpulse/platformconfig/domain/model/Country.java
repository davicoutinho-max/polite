package dev.civicpulse.platformconfig.domain.model;

import java.util.Objects;
import java.util.UUID;

public final class Country {

  private final UUID id;
  private final String name;
  private final String code;

  private Country(UUID id, String name, String code) {
    this.id = Objects.requireNonNull(id);
    this.name = requireNonBlank(name, "name");
    this.code = requireCode(code);
  }

  public static Country create(UUID id, String name, String code) {
    return new Country(id, name, code);
  }

  public static Country reconstitute(UUID id, String name, String code) {
    return new Country(id, name, code);
  }

  private static String requireNonBlank(String value, String field) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(field + " must not be blank");
    }
    return value;
  }

  private static String requireCode(String code) {
    if (code == null || code.length() != 2) {
      throw new IllegalArgumentException("code must be a 2-letter ISO country code");
    }
    return code;
  }

  public UUID id() {
    return id;
  }

  public String name() {
    return name;
  }

  public String code() {
    return code;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Country other)) return false;
    return id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
