package dev.civicpulse.notification.adapter.out.persistence;

import dev.civicpulse.notification.domain.model.NotificationCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Maps only {@code id} as the JPA identifier even though the physical primary key is the
 * composite {@code (id, created_at)} (required by Postgres for range-partitioned tables) — see
 * feed-content-service's {@code PostJpaEntity} for the identical rationale. */
@Entity
@Table(name = "notifications")
public class NotificationJpaEntity {

  @Id private UUID id;

  @Column(name = "recipient_account_id", nullable = false)
  private UUID recipientAccountId;

  @Column(nullable = false)
  private NotificationCategory category;

  private String icon;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private String message;

  private String link;

  @Column(name = "source_event_id", nullable = false)
  private String sourceEventId;

  @Column(nullable = false)
  private boolean read;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected NotificationJpaEntity() {}

  public NotificationJpaEntity(
      UUID id,
      UUID recipientAccountId,
      NotificationCategory category,
      String icon,
      String title,
      String message,
      String link,
      String sourceEventId,
      boolean read,
      Instant createdAt) {
    this.id = id;
    this.recipientAccountId = recipientAccountId;
    this.category = category;
    this.icon = icon;
    this.title = title;
    this.message = message;
    this.link = link;
    this.sourceEventId = sourceEventId;
    this.read = read;
    this.createdAt = createdAt;
  }

  public UUID getId() {
    return id;
  }

  public UUID getRecipientAccountId() {
    return recipientAccountId;
  }

  public NotificationCategory getCategory() {
    return category;
  }

  public String getIcon() {
    return icon;
  }

  public String getTitle() {
    return title;
  }

  public String getMessage() {
    return message;
  }

  public String getLink() {
    return link;
  }

  public String getSourceEventId() {
    return sourceEventId;
  }

  public boolean isRead() {
    return read;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
