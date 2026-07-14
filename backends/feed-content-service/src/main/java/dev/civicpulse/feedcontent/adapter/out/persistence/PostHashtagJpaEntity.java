package dev.civicpulse.feedcontent.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "post_hashtags")
public class PostHashtagJpaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "post_id", nullable = false)
  private UUID postId;

  @Column(nullable = false)
  private String hashtag;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected PostHashtagJpaEntity() {}

  public PostHashtagJpaEntity(Long id, UUID postId, String hashtag, Instant createdAt) {
    this.id = id;
    this.postId = postId;
    this.hashtag = hashtag;
    this.createdAt = createdAt;
  }

  public Long getId() {
    return id;
  }

  public UUID getPostId() {
    return postId;
  }

  public String getHashtag() {
    return hashtag;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
