package dev.civicpulse.feedcontent.domain.model;

import java.util.Objects;
import java.util.UUID;

/** One option of a poll attached to a post — no DB-level FK (posts is partitioned; see
 * schema.sql's comment), referential integrity enforced at the application layer instead. The
 * post's own content is the poll question; a post "has a poll" simply by having option rows. */
public final class PostPollOption {

  private final UUID id;
  private final UUID postId;
  private final String label;
  private final int sortOrder;

  private PostPollOption(UUID id, UUID postId, String label, int sortOrder) {
    this.id = Objects.requireNonNull(id);
    this.postId = Objects.requireNonNull(postId);
    if (label == null || label.isBlank()) {
      throw new IllegalArgumentException("label must not be blank");
    }
    this.label = label;
    this.sortOrder = sortOrder;
  }

  public static PostPollOption create(UUID id, UUID postId, String label, int sortOrder) {
    return new PostPollOption(id, postId, label, sortOrder);
  }

  public static PostPollOption reconstitute(UUID id, UUID postId, String label, int sortOrder) {
    return new PostPollOption(id, postId, label, sortOrder);
  }

  public UUID id() {
    return id;
  }

  public UUID postId() {
    return postId;
  }

  public String label() {
    return label;
  }

  public int sortOrder() {
    return sortOrder;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof PostPollOption other)) return false;
    return id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
