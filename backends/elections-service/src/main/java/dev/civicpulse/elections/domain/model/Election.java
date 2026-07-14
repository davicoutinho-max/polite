package dev.civicpulse.elections.domain.model;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** The election aggregate — a small, mostly-static public calendar entry. No framework imports —
 * the domain core of the hexagonal architecture (see docs/architecture/system-architecture.html). */
public final class Election {

  private final UUID id;
  private final String title;
  private final ElectionScope scope;
  private final LocalDate electionDate;
  private final String description;

  private Election(UUID id, String title, ElectionScope scope, LocalDate electionDate, String description) {
    this.id = Objects.requireNonNull(id);
    this.title = requireNonBlank(title);
    this.scope = Objects.requireNonNull(scope);
    this.electionDate = Objects.requireNonNull(electionDate);
    this.description = description;
  }

  public static Election create(UUID id, String title, ElectionScope scope, LocalDate electionDate, String description) {
    return new Election(id, title, scope, electionDate, description);
  }

  public static Election reconstitute(UUID id, String title, ElectionScope scope, LocalDate electionDate, String description) {
    return new Election(id, title, scope, electionDate, description);
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

  public ElectionScope scope() {
    return scope;
  }

  public LocalDate electionDate() {
    return electionDate;
  }

  public Optional<String> description() {
    return Optional.ofNullable(description);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Election other)) return false;
    return id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
