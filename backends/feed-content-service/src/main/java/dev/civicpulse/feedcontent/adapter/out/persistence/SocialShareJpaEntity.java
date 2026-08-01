package dev.civicpulse.feedcontent.adapter.out.persistence;

import dev.civicpulse.feedcontent.domain.model.ShareStatus;
import dev.civicpulse.feedcontent.domain.model.SocialPlatform;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "social_shares")
public class SocialShareJpaEntity {

  @Id private UUID id;

  @Column(name = "post_id", nullable = false)
  private UUID postId;

  @Column(nullable = false)
  private SocialPlatform platform;

  @Column(nullable = false)
  private ShareStatus status;

  @Column(name = "external_post_id")
  private String externalPostId;

  @Column(name = "error_message")
  private String errorMessage;

  @Column(name = "shared_at", nullable = false)
  private Instant sharedAt;

  protected SocialShareJpaEntity() {}

  public SocialShareJpaEntity(
      UUID id, UUID postId, SocialPlatform platform, ShareStatus status, String externalPostId, String errorMessage, Instant sharedAt) {
    this.id = id;
    this.postId = postId;
    this.platform = platform;
    this.status = status;
    this.externalPostId = externalPostId;
    this.errorMessage = errorMessage;
    this.sharedAt = sharedAt;
  }

  public UUID getId() {
    return id;
  }

  public UUID getPostId() {
    return postId;
  }

  public SocialPlatform getPlatform() {
    return platform;
  }

  public ShareStatus getStatus() {
    return status;
  }

  public String getExternalPostId() {
    return externalPostId;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public Instant getSharedAt() {
    return sharedAt;
  }
}
