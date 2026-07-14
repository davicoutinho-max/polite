package dev.civicpulse.feedcontent.domain.model;

import java.util.Objects;
import java.util.UUID;

/** One-to-one with a {@code kind = 'agenda'} post — no DB-level FK (posts is partitioned; see
 * schema.sql's comment), referential integrity enforced at the application layer instead. */
public final class PostAgendaDetails {

  private final UUID postId;
  private final String title;
  private final String eventDate;
  private final String location;

  private PostAgendaDetails(UUID postId, String title, String eventDate, String location) {
    this.postId = Objects.requireNonNull(postId);
    this.title = requireNonBlank(title, "title");
    this.eventDate = requireNonBlank(eventDate, "eventDate");
    this.location = requireNonBlank(location, "location");
  }

  public static PostAgendaDetails create(UUID postId, String title, String eventDate, String location) {
    return new PostAgendaDetails(postId, title, eventDate, location);
  }

  public static PostAgendaDetails reconstitute(UUID postId, String title, String eventDate, String location) {
    return new PostAgendaDetails(postId, title, eventDate, location);
  }

  private static String requireNonBlank(String value, String field) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(field + " must not be blank");
    }
    return value;
  }

  public UUID postId() {
    return postId;
  }

  public String title() {
    return title;
  }

  public String eventDate() {
    return eventDate;
  }

  public String location() {
    return location;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof PostAgendaDetails other)) return false;
    return postId.equals(other.postId);
  }

  @Override
  public int hashCode() {
    return postId.hashCode();
  }
}
