package dev.civicpulse.feedcontent.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** One attempt to publish a platform post to one external network — kept as a permanent record
 * (including failures, with the reason) rather than only ever storing successes, so a politician
 * can see exactly what happened when a cross-post didn't go through. */
public final class SocialShare {

  private final UUID id;
  private final UUID postId;
  private final SocialPlatform platform;
  private final ShareStatus status;
  private final String externalPostId;
  private final String errorMessage;
  private final Instant sharedAt;

  private SocialShare(
      UUID id, UUID postId, SocialPlatform platform, ShareStatus status, String externalPostId, String errorMessage, Instant sharedAt) {
    this.id = Objects.requireNonNull(id);
    this.postId = Objects.requireNonNull(postId);
    this.platform = Objects.requireNonNull(platform);
    this.status = Objects.requireNonNull(status);
    this.externalPostId = externalPostId;
    this.errorMessage = errorMessage;
    this.sharedAt = Objects.requireNonNull(sharedAt);
  }

  public static SocialShare published(UUID postId, SocialPlatform platform, String externalPostId, Instant now) {
    return new SocialShare(UUID.randomUUID(), postId, platform, ShareStatus.PUBLISHED, externalPostId, null, now);
  }

  public static SocialShare failed(UUID postId, SocialPlatform platform, String errorMessage, Instant now) {
    return new SocialShare(UUID.randomUUID(), postId, platform, ShareStatus.FAILED, null, errorMessage, now);
  }

  public static SocialShare reconstitute(
      UUID id, UUID postId, SocialPlatform platform, ShareStatus status, String externalPostId, String errorMessage, Instant sharedAt) {
    return new SocialShare(id, postId, platform, status, externalPostId, errorMessage, sharedAt);
  }

  public UUID id() {
    return id;
  }

  public UUID postId() {
    return postId;
  }

  public SocialPlatform platform() {
    return platform;
  }

  public ShareStatus status() {
    return status;
  }

  public Optional<String> externalPostId() {
    return Optional.ofNullable(externalPostId);
  }

  public Optional<String> errorMessage() {
    return Optional.ofNullable(errorMessage);
  }

  public Instant sharedAt() {
    return sharedAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SocialShare other)) return false;
    return id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
