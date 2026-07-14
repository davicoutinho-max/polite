package dev.civicpulse.feedcontent.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Denormalized counters — updated synchronously by this service in this implementation.
 * schema.sql documents an async-via-Redis-then-reconciled design for scale; the counters
 * themselves are always correct either way, this is a latency/throughput trade-off, not a
 * correctness one (see the same note on {@link Like}). */
public final class PostMetrics {

  private final UUID postId;
  private int likesCount;
  private int commentsCount;
  private int sharesCount;
  private Instant updatedAt;

  private PostMetrics(UUID postId, int likesCount, int commentsCount, int sharesCount, Instant updatedAt) {
    this.postId = Objects.requireNonNull(postId);
    this.likesCount = likesCount;
    this.commentsCount = commentsCount;
    this.sharesCount = sharesCount;
    this.updatedAt = Objects.requireNonNull(updatedAt);
  }

  public static PostMetrics initial(UUID postId, Instant now) {
    return new PostMetrics(postId, 0, 0, 0, now);
  }

  public static PostMetrics reconstitute(UUID postId, int likesCount, int commentsCount, int sharesCount, Instant updatedAt) {
    return new PostMetrics(postId, likesCount, commentsCount, sharesCount, updatedAt);
  }

  public void incrementLikes(Instant now) {
    this.likesCount++;
    this.updatedAt = now;
  }

  public void decrementLikes(Instant now) {
    this.likesCount = Math.max(0, this.likesCount - 1);
    this.updatedAt = now;
  }

  public void incrementComments(Instant now) {
    this.commentsCount++;
    this.updatedAt = now;
  }

  public UUID postId() {
    return postId;
  }

  public int likesCount() {
    return likesCount;
  }

  public int commentsCount() {
    return commentsCount;
  }

  public int sharesCount() {
    return sharesCount;
  }

  public Instant updatedAt() {
    return updatedAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof PostMetrics other)) return false;
    return postId.equals(other.postId);
  }

  @Override
  public int hashCode() {
    return postId.hashCode();
  }
}
