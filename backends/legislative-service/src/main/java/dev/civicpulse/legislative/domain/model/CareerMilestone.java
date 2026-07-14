package dev.civicpulse.legislative.domain.model;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class CareerMilestone {

  private final UUID id;
  private final UUID politicianAccountId;
  private final short year;
  private final String title;
  private final String detail;

  private CareerMilestone(UUID id, UUID politicianAccountId, short year, String title, String detail) {
    this.id = id;
    this.politicianAccountId = Objects.requireNonNull(politicianAccountId);
    this.year = year;
    this.title = requireNonBlank(title, "title");
    this.detail = detail;
  }

  public static CareerMilestone add(UUID politicianAccountId, short year, String title, String detail) {
    return new CareerMilestone(null, politicianAccountId, year, title, detail);
  }

  public static CareerMilestone reconstitute(UUID id, UUID politicianAccountId, short year, String title, String detail) {
    return new CareerMilestone(id, politicianAccountId, year, title, detail);
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

  public short year() {
    return year;
  }

  public String title() {
    return title;
  }

  public Optional<String> detail() {
    return Optional.ofNullable(detail);
  }
}
