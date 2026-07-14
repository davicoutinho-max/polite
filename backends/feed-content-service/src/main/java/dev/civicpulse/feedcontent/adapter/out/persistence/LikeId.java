package dev.civicpulse.feedcontent.adapter.out.persistence;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class LikeId implements Serializable {

  private UUID postId;
  private UUID accountId;

  public LikeId() {}

  public LikeId(UUID postId, UUID accountId) {
    this.postId = postId;
    this.accountId = accountId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof LikeId other)) return false;
    return Objects.equals(postId, other.postId) && Objects.equals(accountId, other.accountId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(postId, accountId);
  }
}
