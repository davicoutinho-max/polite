package dev.civicpulse.feedcontent.adapter.out.persistence;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class CommentLikeId implements Serializable {

  private UUID commentId;
  private UUID accountId;

  public CommentLikeId() {}

  public CommentLikeId(UUID commentId, UUID accountId) {
    this.commentId = commentId;
    this.accountId = accountId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof CommentLikeId other)) return false;
    return Objects.equals(commentId, other.commentId) && Objects.equals(accountId, other.accountId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(commentId, accountId);
  }
}
