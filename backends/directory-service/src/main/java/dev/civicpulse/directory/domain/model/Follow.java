package dev.civicpulse.directory.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** A social-graph edge: {@code followerAccountId} follows a politician or party. The only
 * aggregate in this service actually written by end users — {@link Politician} and
 * {@link Party} are read-only projections. */
public final class Follow {

  private final UUID followerAccountId;
  private final FollowTargetType targetType;
  private final UUID targetId;
  private final Instant createdAt;

  private Follow(UUID followerAccountId, FollowTargetType targetType, UUID targetId, Instant createdAt) {
    this.followerAccountId = Objects.requireNonNull(followerAccountId);
    this.targetType = Objects.requireNonNull(targetType);
    this.targetId = Objects.requireNonNull(targetId);
    this.createdAt = Objects.requireNonNull(createdAt);
  }

  public static Follow create(UUID followerAccountId, FollowTargetType targetType, UUID targetId, Instant now) {
    return new Follow(followerAccountId, targetType, targetId, now);
  }

  public static Follow reconstitute(UUID followerAccountId, FollowTargetType targetType, UUID targetId, Instant createdAt) {
    return new Follow(followerAccountId, targetType, targetId, createdAt);
  }

  public UUID followerAccountId() {
    return followerAccountId;
  }

  public FollowTargetType targetType() {
    return targetType;
  }

  public UUID targetId() {
    return targetId;
  }

  public Instant createdAt() {
    return createdAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Follow other)) return false;
    return followerAccountId.equals(other.followerAccountId) && targetType == other.targetType && targetId.equals(other.targetId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(followerAccountId, targetType, targetId);
  }
}
