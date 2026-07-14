package dev.civicpulse.feedcontent.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Hot table by design (see schema.sql) — a real deployment would absorb the write burst in
 * Redis with async flush; this service writes straight to Postgres, which is correct, just not
 * yet fast at scale (see the same trade-off noted for {@code posts}' partitioning). */
public final class Like {

  private final UUID postId;
  private final UUID accountId;
  private final Instant createdAt;

  private Like(UUID postId, UUID accountId, Instant createdAt) {
    this.postId = Objects.requireNonNull(postId);
    this.accountId = Objects.requireNonNull(accountId);
    this.createdAt = Objects.requireNonNull(createdAt);
  }

  public static Like create(UUID postId, UUID accountId, Instant now) {
    return new Like(postId, accountId, now);
  }

  public static Like reconstitute(UUID postId, UUID accountId, Instant createdAt) {
    return new Like(postId, accountId, createdAt);
  }

  public UUID postId() {
    return postId;
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
    if (!(o instanceof Like other)) return false;
    return postId.equals(other.postId) && accountId.equals(other.accountId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(postId, accountId);
  }
}
