package dev.civicpulse.feedcontent.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "post_metrics")
public class PostMetricsJpaEntity {

  @Id
  @Column(name = "post_id")
  private UUID postId;

  @Column(name = "likes_count", nullable = false)
  private int likesCount;

  @Column(name = "comments_count", nullable = false)
  private int commentsCount;

  @Column(name = "shares_count", nullable = false)
  private int sharesCount;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected PostMetricsJpaEntity() {}

  public PostMetricsJpaEntity(UUID postId, int likesCount, int commentsCount, int sharesCount, Instant updatedAt) {
    this.postId = postId;
    this.likesCount = likesCount;
    this.commentsCount = commentsCount;
    this.sharesCount = sharesCount;
    this.updatedAt = updatedAt;
  }

  public UUID getPostId() {
    return postId;
  }

  public int getLikesCount() {
    return likesCount;
  }

  public int getCommentsCount() {
    return commentsCount;
  }

  public int getSharesCount() {
    return sharesCount;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
