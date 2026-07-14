package dev.civicpulse.participation.domain.model;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** No framework imports — the domain core of the hexagonal architecture (see
 * docs/architecture/system-architecture.html). */
public final class Petition {

  private final UUID id;
  private final String title;
  private final String summary;
  private final String category;
  private final int goal;
  private int signaturesCount;
  private final LocalDate deadline;

  private Petition(UUID id, String title, String summary, String category, int goal, int signaturesCount, LocalDate deadline) {
    this.id = Objects.requireNonNull(id);
    this.title = requireNonBlank(title);
    this.summary = summary;
    this.category = category;
    if (goal <= 0) {
      throw new IllegalArgumentException("goal must be positive");
    }
    this.goal = goal;
    this.signaturesCount = signaturesCount;
    this.deadline = deadline;
  }

  public static Petition create(UUID id, String title, String summary, String category, int goal, LocalDate deadline) {
    return new Petition(id, title, summary, category, goal, 0, deadline);
  }

  public static Petition reconstitute(UUID id, String title, String summary, String category, int goal, int signaturesCount, LocalDate deadline) {
    return new Petition(id, title, summary, category, goal, signaturesCount, deadline);
  }

  public void recordSignature() {
    signaturesCount++;
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

  public Optional<String> summary() {
    return Optional.ofNullable(summary);
  }

  public Optional<String> category() {
    return Optional.ofNullable(category);
  }

  public int goal() {
    return goal;
  }

  public int signaturesCount() {
    return signaturesCount;
  }

  public Optional<LocalDate> deadline() {
    return Optional.ofNullable(deadline);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Petition other)) return false;
    return id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
