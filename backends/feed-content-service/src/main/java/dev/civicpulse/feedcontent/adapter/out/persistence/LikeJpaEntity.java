package dev.civicpulse.feedcontent.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "likes")
@IdClass(LikeId.class)
public class LikeJpaEntity {

  @Id
  @Column(name = "post_id")
  private UUID postId;

  @Id
  @Column(name = "account_id")
  private UUID accountId;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected LikeJpaEntity() {}

  public LikeJpaEntity(UUID postId, UUID accountId, Instant createdAt) {
    this.postId = postId;
    this.accountId = accountId;
    this.createdAt = createdAt;
  }

  public UUID getPostId() {
    return postId;
  }

  public UUID getAccountId() {
    return accountId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
