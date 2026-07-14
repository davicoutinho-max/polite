package dev.civicpulse.analytics.adapter.out.persistence;

import dev.civicpulse.analytics.domain.model.EngagementEventType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "engagement_events")
public class EngagementEventJpaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "author_account_id", nullable = false)
  private UUID authorAccountId;

  @Column(name = "actor_account_id", nullable = false)
  private UUID actorAccountId;

  @Column(name = "actor_account_type")
  private String actorAccountType;

  @Column(name = "event_type", nullable = false)
  private EngagementEventType eventType;

  @Column(name = "content_type")
  private String contentType;

  @Column(name = "occurred_at", nullable = false)
  private Instant occurredAt;

  @Column(name = "source_event_id", nullable = false)
  private String sourceEventId;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected EngagementEventJpaEntity() {}

  public EngagementEventJpaEntity(
      Long id,
      UUID authorAccountId,
      UUID actorAccountId,
      String actorAccountType,
      EngagementEventType eventType,
      String contentType,
      Instant occurredAt,
      String sourceEventId,
      Instant createdAt) {
    this.id = id;
    this.authorAccountId = authorAccountId;
    this.actorAccountId = actorAccountId;
    this.actorAccountType = actorAccountType;
    this.eventType = eventType;
    this.contentType = contentType;
    this.occurredAt = occurredAt;
    this.sourceEventId = sourceEventId;
    this.createdAt = createdAt;
  }

  public Long getId() {
    return id;
  }

  public UUID getAuthorAccountId() {
    return authorAccountId;
  }

  public UUID getActorAccountId() {
    return actorAccountId;
  }

  public String getActorAccountType() {
    return actorAccountType;
  }

  public EngagementEventType getEventType() {
    return eventType;
  }

  public String getContentType() {
    return contentType;
  }

  public Instant getOccurredAt() {
    return occurredAt;
  }

  public String getSourceEventId() {
    return sourceEventId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
