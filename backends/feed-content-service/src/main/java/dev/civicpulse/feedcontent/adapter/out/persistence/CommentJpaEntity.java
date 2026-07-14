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

  @Column(nullable = false)
  private String body;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected CommentJpaEntity() {}

  public CommentJpaEntity(UUID id, UUID postId, UUID authorAccountId, String body, Instant createdAt) {
    this.id = id;
    this.postId = postId;
    this.authorAccountId = authorAccountId;
    this.body = body;
    this.createdAt = createdAt;
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

  public String getBody() {
    return body;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
