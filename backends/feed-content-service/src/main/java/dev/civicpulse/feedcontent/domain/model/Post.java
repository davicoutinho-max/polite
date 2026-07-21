package dev.civicpulse.feedcontent.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** The durable Post aggregate — Postgres is the system of record; Redis (documented in
 * schema.sql, not implemented in this pass — see the service-level note in
 * FeedContentServiceApplication) serves the ranked/hot read path on top of it. No framework
 * imports — the domain core of the hexagonal architecture (see
 * docs/architecture/system-architecture.html). */
public final class Post {

  private final UUID id;
  private final UUID authorAccountId;
  private final PostKind kind;
  private final String content;
  private final String imageUrl;
  private final String fileUrl;
  private final String fileName;
  private final PostVisibility visibility;
  private final String context;
  private final UUID liveSessionId;
  private final Instant createdAt;

  private Post(
      UUID id,
      UUID authorAccountId,
      PostKind kind,
      String content,
      String imageUrl,
      String fileUrl,
      String fileName,
      PostVisibility visibility,
      String context,
      UUID liveSessionId,
      Instant createdAt) {
    this.id = Objects.requireNonNull(id);
    this.authorAccountId = Objects.requireNonNull(authorAccountId);
    this.kind = Objects.requireNonNull(kind);
    this.content = content;
    this.imageUrl = imageUrl;
    this.fileUrl = fileUrl;
    this.fileName = fileName;
    this.visibility = Objects.requireNonNull(visibility);
    this.context = context;
    this.liveSessionId = liveSessionId;
    this.createdAt = Objects.requireNonNull(createdAt);
  }

  public static Post publish(
      UUID id,
      UUID authorAccountId,
      PostKind kind,
      String content,
      String imageUrl,
      String fileUrl,
      String fileName,
      PostVisibility visibility,
      String context,
      UUID liveSessionId,
      Instant now) {
    return new Post(id, authorAccountId, kind, content, imageUrl, fileUrl, fileName, visibility, context, liveSessionId, now);
  }

  public static Post reconstitute(
      UUID id,
      UUID authorAccountId,
      PostKind kind,
      String content,
      String imageUrl,
      String fileUrl,
      String fileName,
      PostVisibility visibility,
      String context,
      UUID liveSessionId,
      Instant createdAt) {
    return new Post(id, authorAccountId, kind, content, imageUrl, fileUrl, fileName, visibility, context, liveSessionId, createdAt);
  }

  public UUID id() {
    return id;
  }

  public UUID authorAccountId() {
    return authorAccountId;
  }

  public PostKind kind() {
    return kind;
  }

  public Optional<String> content() {
    return Optional.ofNullable(content);
  }

  public Optional<String> imageUrl() {
    return Optional.ofNullable(imageUrl);
  }

  public Optional<String> fileUrl() {
    return Optional.ofNullable(fileUrl);
  }

  public Optional<String> fileName() {
    return Optional.ofNullable(fileName);
  }

  public PostVisibility visibility() {
    return visibility;
  }

  public Optional<String> context() {
    return Optional.ofNullable(context);
  }

  public Optional<UUID> liveSessionId() {
    return Optional.ofNullable(liveSessionId);
  }

  public Instant createdAt() {
    return createdAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Post other)) return false;
    return id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
