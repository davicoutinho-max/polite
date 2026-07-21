package dev.civicpulse.feedcontent.adapter.out.persistence;

import dev.civicpulse.feedcontent.domain.model.PostKind;
import dev.civicpulse.feedcontent.domain.model.PostVisibility;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Maps only {@code id} as the JPA identifier even though the physical primary key is the
 * composite {@code (id, created_at)} (required by Postgres for range-partitioned tables) —
 * {@code id} alone is already globally unique (a UUID), so {@code findById}/updates via it are
 * correct; only the DDL-level constraint is composite, which Hibernate's schema validator
 * doesn't police at that granularity. */
@Entity
@Table(name = "posts")
public class PostJpaEntity {

  @Id private UUID id;

  @Column(name = "author_account_id", nullable = false)
  private UUID authorAccountId;

  @Column(nullable = false)
  private PostKind kind;

  private String content;

  @Column(name = "image_url")
  private String imageUrl;

  @Column(name = "file_url")
  private String fileUrl;

  @Column(name = "file_name")
  private String fileName;

  @Column(nullable = false)
  private PostVisibility visibility;

  private String context;

  @Column(name = "live_session_id")
  private UUID liveSessionId;

  @Column(name = "created_at")
  private Instant createdAt;

  protected PostJpaEntity() {}

  public PostJpaEntity(
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
    this.id = id;
    this.authorAccountId = authorAccountId;
    this.kind = kind;
    this.content = content;
    this.imageUrl = imageUrl;
    this.fileUrl = fileUrl;
    this.fileName = fileName;
    this.visibility = visibility;
    this.context = context;
    this.liveSessionId = liveSessionId;
    this.createdAt = createdAt;
  }

  public UUID getId() {
    return id;
  }

  public UUID getAuthorAccountId() {
    return authorAccountId;
  }

  public PostKind getKind() {
    return kind;
  }

  public String getContent() {
    return content;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public String getFileUrl() {
    return fileUrl;
  }

  public String getFileName() {
    return fileName;
  }

  public PostVisibility getVisibility() {
    return visibility;
  }

  public String getContext() {
    return context;
  }

  public UUID getLiveSessionId() {
    return liveSessionId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
