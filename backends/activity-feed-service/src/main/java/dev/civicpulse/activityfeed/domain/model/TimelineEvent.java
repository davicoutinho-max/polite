package dev.civicpulse.activityfeed.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** A single entry in a profile's activity timeline. Framework-free domain core — see
 * schema.sql's table comment for why "group" (Today/Yesterday/This week) is never stored here. */
public final class TimelineEvent {

  private final UUID id;
  private final UUID subjectAccountId;
  private final TimelineEventType type;
  private final String title;
  private final String detail;
  private final Instant occurredAt;
  private final String sourceEventId;
  private final UUID actorAccountId;
  private final String actorNameDenormalized;
  private final Instant createdAt;

  private TimelineEvent(
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
    this.subjectAccountId = Objects.requireNonNull(subjectAccountId);
    this.type = Objects.requireNonNull(type);
    this.title = requireNonBlank(title, "title");
    this.detail = detail;
    this.occurredAt = Objects.requireNonNull(occurredAt);
    this.sourceEventId = requireNonBlank(sourceEventId, "sourceEventId");
    this.actorAccountId = Objects.requireNonNull(actorAccountId);
    this.actorNameDenormalized = actorNameDenormalized;
    this.createdAt = Objects.requireNonNull(createdAt);
  }

  public static TimelineEvent record(
      UUID subjectAccountId,
      TimelineEventType type,
      String title,
      String detail,
      Instant occurredAt,
      String sourceEventId,
      UUID actorAccountId,
      String actorNameDenormalized,
      Instant now) {
    return new TimelineEvent(null, subjectAccountId, type, title, detail, occurredAt, sourceEventId, actorAccountId, actorNameDenormalized, now);
  }

  public static TimelineEvent reconstitute(
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
    return new TimelineEvent(id, subjectAccountId, type, title, detail, occurredAt, sourceEventId, actorAccountId, actorNameDenormalized, createdAt);
  }

  private static String requireNonBlank(String value, String field) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(field + " must not be blank");
    }
    return value;
  }

  public Optional<UUID> id() {
    return Optional.ofNullable(id);
  }

  public UUID subjectAccountId() {
    return subjectAccountId;
  }

  public TimelineEventType type() {
    return type;
  }

  public String title() {
    return title;
  }

  public Optional<String> detail() {
    return Optional.ofNullable(detail);
  }

  public Instant occurredAt() {
    return occurredAt;
  }

  public String sourceEventId() {
    return sourceEventId;
  }

  public UUID actorAccountId() {
    return actorAccountId;
  }

  public Optional<String> actorNameDenormalized() {
    return Optional.ofNullable(actorNameDenormalized);
  }

  public Instant createdAt() {
    return createdAt;
  }
}
