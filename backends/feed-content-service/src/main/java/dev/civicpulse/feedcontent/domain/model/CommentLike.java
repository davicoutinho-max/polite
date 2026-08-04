package dev.civicpulse.feedcontent.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Mirrors {@link Like} (post likes) but keyed by comment instead of post — see that class's
 * javadoc for the write-volume trade-off, which applies here too. */
public final class CommentLike {

  private final UUID commentId;
  private final UUID accountId;
  private final Instant createdAt;

  private CommentLike(UUID commentId, UUID accountId, Instant createdAt) {
    this.commentId = Objects.requireNonNull(commentId);
    this.accountId = Objects.requireNonNull(accountId);
    this.createdAt = Objects.requireNonNull(createdAt);
  }

  public static CommentLike create(UUID commentId, UUID accountId, Instant now) {
    return new CommentLike(commentId, accountId, now);
  }

  public static CommentLike reconstitute(UUID commentId, UUID accountId, Instant createdAt) {
    return new CommentLike(commentId, accountId, createdAt);
  }

  public UUID commentId() {
    return commentId;
  }

  public UUID accountId() {
    return accountId;
  }

  public Instant createdAt() {
    return createdAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof CommentLike other)) return false;
    return commentId.equals(other.commentId) && accountId.equals(other.accountId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(commentId, accountId);
  }
}
