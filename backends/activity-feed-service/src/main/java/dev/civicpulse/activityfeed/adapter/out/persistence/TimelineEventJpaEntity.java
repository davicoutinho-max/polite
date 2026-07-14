package dev.civicpulse.activityfeed.adapter.out.persistence;

import dev.civicpulse.activityfeed.domain.model.TimelineEventType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "timeline_events")
public class TimelineEventJpaEntity {

  @Id
  @GeneratedValue
  private UUID id;

  @Column(name = "subject_account_id", nullable = false)
  private UUID subjectAccountId;

  @Column(nullable = false)
  private TimelineEventType type;

  @Column(nullable = false)
  private String title;

  private String detail;

  @Column(name = "occurred_at", nullable = false)
  private Instant occurredAt;

  @Column(name = "source_event_id", nullable = false)
  private String sourceEventId;

  @Column(name = "actor_account_id", nullable = false)
  private UUID actorAccountId;

  @Column(name = "actor_name_denormalized")
  private String actorNameDenormalized;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected TimelineEventJpaEntity() {}

  public TimelineEventJpaEntity(
      UUID id,
      UUID subjectAccountId,
      TimelineEventType type,
      String title,
      String detail,
      Instant occurredAt,
      String sourceEventId,
      UUID actorAccountId,
      String actorNameDenormalized,
      Instant createdAt) {
    this.id = id;
    this.subjectAccountId = subjectAccountId;
    this.type = type;
    this.title = title;
    this.detail = detail;
    this.occurredAt = occurredAt;
    this.sourceEventId = sourceEventId;
    this.actorAccountId = actorAccountId;
    this.actorNameDenormalized = actorNameDenormalized;
    this.createdAt = createdAt;
  }

  public UUID getId() {
    return id;
  }

  public UUID getSubjectAccountId() {
    return subjectAccountId;
  }

  public TimelineEventType getType() {
    return type;
  }

  public String getTitle() {
    return title;
  }

  public String getDetail() {
    return detail;
  }

  public Instant getOccurredAt() {
    return occurredAt;
  }

  public String getSourceEventId() {
    return sourceEventId;
  }

  public UUID getActorAccountId() {
    return actorAccountId;
  }

  public String getActorNameDenormalized() {
    return actorNameDenormalized;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
