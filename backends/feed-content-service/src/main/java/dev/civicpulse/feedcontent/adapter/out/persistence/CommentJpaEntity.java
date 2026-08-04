package dev.civicpulse.feedcontent.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "comments")
public class CommentJpaEntity {

  @Id private UUID id;

  @Column(name = "post_id", nullable = false)
  private UUID postId;

  @Column(name = "author_account_id", nullable = false)
  private UUID authorAccountId;

  @Column(name = "parent_comment_id")
  private UUID parentCommentId;

  @Column(nullable = false)
  private String body;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "likes_count", nullable = false)
  private int likesCount;

  protected CommentJpaEntity() {}

  public CommentJpaEntity(
      UUID id,
      UUID postId,
      UUID authorAccountId,
      UUID parentCommentId,
      String body,
      Instant createdAt,
      int likesCount) {
    this.id = id;
    this.postId = postId;
    this.authorAccountId = authorAccountId;
    this.parentCommentId = parentCommentId;
    this.body = body;
    this.createdAt = createdAt;
    this.likesCount = likesCount;
  }

  public UUID getId() {
    return id;
  }

  public UUID getPostId() {
    return postId;
  }

  public UUID getAuthorAccountId() {
    return authorAccountId;
  }

  public UUID getParentCommentId() {
    return parentCommentId;
  }

  public String getBody() {
    return body;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public int getLikesCount() {
    return likesCount;
  }
}
