package dev.civicpulse.directory.adapter.out.persistence;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class FollowId implements Serializable {

  private UUID followerAccountId;
  private String targetType;
  private UUID targetId;

  public FollowId() {}

  public FollowId(UUID followerAccountId, String targetType, UUID targetId) {
    this.followerAccountId = followerAccountId;
    this.targetType = targetType;
    this.targetId = targetId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof FollowId other)) return false;
    return Objects.equals(followerAccountId, other.followerAccountId)
        && Objects.equals(targetType, other.targetType)
        && Objects.equals(targetId, other.targetId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(followerAccountId, targetType, targetId);
  }
}
