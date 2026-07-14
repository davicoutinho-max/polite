package dev.civicpulse.feedcontent.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class Comment {

  private final UUID id;
  private final UUID postId;
  private final UUID authorAccountId;
  private final String body;
  private final Instant createdAt;

  private Comment(UUID id, UUID postId, UUID authorAccountId, String body, Instant createdAt) {
    this.id = Objects.requireNonNull(id);
    this.postId = Objects.requireNonNull(postId);
    this.authorAccountId = Objects.requireNonNull(authorAccountId);
    this.body = requireNonBlank(body);
    this.createdAt = Objects.requireNonNull(createdAt);
  }

  public static Comment add(UUID id, UUID postId, UUID authorAccountId, String body, Instant now) {
    return new Comment(id, postId, authorAccountId, body, now);
  }

  public static Comment reconstitute(UUID id, UUID postId, UUID authorAccountId, String body, Instant createdAt) {
    return new Comment(id, postId, authorAccountId, body, createdAt);
  }

  private static String requireNonBlank(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("body must not be blank");
    }
    return value;
  }

  public UUID id() {
    return id;
  }

  public UUID postId() {
    return postId;
  }

  public UUID authorAccountId() {
    return authorAccountId;
  }

  public String body() {
    return body;
  }

  public Instant createdAt() {
    return createdAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Comment other)) return false;
    return id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
