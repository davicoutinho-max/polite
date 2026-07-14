package dev.civicpulse.legislative.domain.model;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class Mandate {

  private final UUID id;
  private final UUID politicianAccountId;
  private final String role;
  private final String period;
  private final boolean current;

  private Mandate(UUID id, UUID politicianAccountId, String role, String period, boolean current) {
    this.id = id;
    this.politicianAccountId = Objects.requireNonNull(politicianAccountId);
    this.role = requireNonBlank(role, "role");
    this.period = requireNonBlank(period, "period");
    this.current = current;
  }

  public static Mandate add(UUID politicianAccountId, String role, String period, boolean current) {
    return new Mandate(null, politicianAccountId, role, period, current);
  }

  public static Mandate reconstitute(UUID id, UUID politicianAccountId, String role, String period, boolean current) {
    return new Mandate(id, politicianAccountId, role, period, current);
  }

  private static String requireNonBlank(String value, String field) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(field + " must not be blank");
    }
    return value;
  }

  public Optional<UUID> id() {
    return Optional.ofNullable(id);
  }

  public UUID politicianAccountId() {
    return politicianAccountId;
  }

  public String role() {
    return role;
  }

  public String period() {
    return period;
  }

  public boolean current() {
    return current;
  }
}
