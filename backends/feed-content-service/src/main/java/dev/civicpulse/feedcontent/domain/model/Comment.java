package dev.civicpulse.feedcontent.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class Comment {

  private final UUID id;
  private final UUID postId;
  private final UUID authorAccountId;
  private final UUID parentCommentId;
  private final String body;
  private final Instant createdAt;
  private int likesCount;

  private Comment(
      UUID id,
      UUID postId,
      UUID authorAccountId,
      UUID parentCommentId,
      String body,
      Instant createdAt,
      int likesCount) {
    this.id = Objects.requireNonNull(id);
    this.postId = Objects.requireNonNull(postId);
    this.authorAccountId = Objects.requireNonNull(authorAccountId);
    this.parentCommentId = parentCommentId;
    this.body = requireNonBlank(body);
    this.createdAt = Objects.requireNonNull(createdAt);
    this.likesCount = likesCount;
  }

  /** {@code parentCommentId} is null for a top-level comment, or the id of the comment being
   * replied to — one level of threading only, replies-to-replies still attach to the original
   * top-level comment (see ManageCommentUseCase javadoc). */
  public static Comment add(UUID id, UUID postId, UUID authorAccountId, UUID parentCommentId, String body, Instant now) {
    return new Comment(id, postId, authorAccountId, parentCommentId, body, now, 0);
  }

  public static Comment reconstitute(
      UUID id,
      UUID postId,
      UUID authorAccountId,
      UUID parentCommentId,
      String body,
      Instant createdAt,
      int likesCount) {
    return new Comment(id, postId, authorAccountId, parentCommentId, body, createdAt, likesCount);
  }

  private static String requireNonBlank(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("body must not be blank");
    }
    return value;
  }

  public void incrementLikes() {
    likesCount++;
  }

  public void decrementLikes() {
    likesCount = Math.max(0, likesCount - 1);
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

  public UUID parentCommentId() {
    return parentCommentId;
  }

  public String body() {
    return body;
  }

  public Instant createdAt() {
    return createdAt;
  }

  public int likesCount() {
    return likesCount;
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
