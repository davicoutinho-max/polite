package dev.civicpulse.platformconfig.domain.model;

import java.util.Objects;
import java.util.UUID;

public final class State {

  private final UUID id;
  private final UUID countryId;
  private final String name;
  private final String code;

  private State(UUID id, UUID countryId, String name, String code) {
    this.id = Objects.requireNonNull(id);
    this.countryId = Objects.requireNonNull(countryId);
    this.name = requireNonBlank(name, "name");
    this.code = requireNonBlank(code, "code");
  }

  public static State create(UUID id, UUID countryId, String name, String code) {
    return new State(id, countryId, name, code);
  }

  public static State reconstitute(UUID id, UUID countryId, String name, String code) {
    return new State(id, countryId, name, code);
  }

  private static String requireNonBlank(String value, String field) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(field + " must not be blank");
    }
    return value;
  }

  public UUID id() {
    return id;
  }

  public UUID countryId() {
    return countryId;
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
    if (!(o instanceof State other)) return false;
    return id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
