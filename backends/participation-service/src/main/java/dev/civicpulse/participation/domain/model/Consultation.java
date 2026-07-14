package dev.civicpulse.participation.domain.model;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class Consultation {

  private final UUID id;
  private final String title;
  private final String description;
  private final LocalDate deadline;
  private int responsesCount;

  private Consultation(UUID id, String title, String description, LocalDate deadline, int responsesCount) {
    this.id = Objects.requireNonNull(id);
    this.title = requireNonBlank(title);
    this.description = description;
    this.deadline = deadline;
    this.responsesCount = responsesCount;
  }

  public static Consultation create(UUID id, String title, String description, LocalDate deadline) {
    return new Consultation(id, title, description, deadline, 0);
  }

  public static Consultation reconstitute(UUID id, String title, String description, LocalDate deadline, int responsesCount) {
    return new Consultation(id, title, description, deadline, responsesCount);
  }

  /** Never double-counts {@code responsesCount} on a second write — a citizen changing their
   * stance updates their existing response in place (see schema.sql's comment on
   * {@code consultation_responses}); only call this the first time a citizen responds. */
  public void recordNewResponse() {
    responsesCount++;
  }

  private static String requireNonBlank(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("title must not be blank");
    }
    return value;
  }

  public UUID id() {
    return id;
  }

  public String title() {
    return title;
  }

  public Optional<String> description() {
    return Optional.ofNullable(description);
  }

  public Optional<LocalDate> deadline() {
    return Optional.ofNullable(deadline);
  }

  public int responsesCount() {
    return responsesCount;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Consultation other)) return false;
    return id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
