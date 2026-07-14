package dev.civicpulse.feedcontent.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class PostHashtag {

  private final Long id;
  private final UUID postId;
  private final String hashtag;
  private final Instant createdAt;

  private PostHashtag(Long id, UUID postId, String hashtag, Instant createdAt) {
    this.id = id;
    this.postId = Objects.requireNonNull(postId);
    this.hashtag = requireNonBlank(hashtag);
    this.createdAt = Objects.requireNonNull(createdAt);
  }

  public static PostHashtag add(UUID postId, String hashtag, Instant createdAt) {
    return new PostHashtag(null, postId, hashtag, createdAt);
  }

  public static PostHashtag reconstitute(Long id, UUID postId, String hashtag, Instant createdAt) {
    return new PostHashtag(id, postId, hashtag, createdAt);
  }

  private static String requireNonBlank(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("hashtag must not be blank");
    }
    return value;
  }

  public Optional<Long> id() {
    return Optional.ofNullable(id);
  }

  public UUID postId() {
    return postId;
  }

  public String hashtag() {
    return hashtag;
  }

  public Instant createdAt() {
    return createdAt;
  }
}
