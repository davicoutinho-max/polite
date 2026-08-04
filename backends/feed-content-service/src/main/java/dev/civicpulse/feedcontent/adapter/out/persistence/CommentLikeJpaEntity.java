package dev.civicpulse.feedcontent.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "comment_likes")
@IdClass(CommentLikeId.class)
public class CommentLikeJpaEntity {

  @Id
  @Column(name = "comment_id")
  private UUID commentId;

  @Id
  @Column(name = "account_id")
  private UUID accountId;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected CommentLikeJpaEntity() {}

  public CommentLikeJpaEntity(UUID commentId, UUID accountId, Instant createdAt) {
    this.commentId = commentId;
    this.accountId = accountId;
    this.createdAt = createdAt;
  }

  public UUID getCommentId() {
    return commentId;
  }

  public UUID getAccountId() {
    return accountId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
