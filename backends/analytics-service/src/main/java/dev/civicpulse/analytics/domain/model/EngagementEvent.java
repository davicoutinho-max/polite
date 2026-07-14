package dev.civicpulse.analytics.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** A single raw interaction fact — every read (KPIs, engagement rate, by-content-type,
 * by-account-type) is a query against a collection of these, computed at read time, see
 * schema.sql's table comment for why no pre-aggregated cache exists for this pass. */
public final class EngagementEvent {

  private final Long id;
  private final UUID authorAccountId;
  private final UUID actorAccountId;
  private final String actorAccountType;
  private final EngagementEventType eventType;
  private final String contentType;
  private final Instant occurredAt;
  private final String sourceEventId;
  private final Instant createdAt;

  private EngagementEvent(
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
    this.authorAccountId = Objects.requireNonNull(authorAccountId);
    this.actorAccountId = Objects.requireNonNull(actorAccountId);
    this.actorAccountType = actorAccountType;
    this.eventType = Objects.requireNonNull(eventType);
    this.contentType = contentType;
    this.occurredAt = Objects.requireNonNull(occurredAt);
    this.sourceEventId = requireNonBlank(sourceEventId);
    this.createdAt = Objects.requireNonNull(createdAt);
  }

  public static EngagementEvent record(
      UUID authorAccountId,
      UUID actorAccountId,
      String actorAccountType,
      EngagementEventType eventType,
      String contentType,
      Instant occurredAt,
      String sourceEventId,
      Instant now) {
    return new EngagementEvent(null, authorAccountId, actorAccountId, actorAccountType, eventType, contentType, occurredAt, sourceEventId, now);
  }

  public static EngagementEvent reconstitute(
      Long id,
      UUID authorAccountId,
      UUID actorAccountId,
      String actorAccountType,
      EngagementEventType eventType,
      String contentType,
      Instant occurredAt,
      String sourceEventId,
      Instant createdAt) {
    return new EngagementEvent(id, authorAccountId, actorAccountId, actorAccountType, eventType, contentType, occurredAt, sourceEventId, createdAt);
  }

  private static String requireNonBlank(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("sourceEventId must not be blank");
    }
    return value;
  }

  public Optional<Long> id() {
    return Optional.ofNullable(id);
  }

  public UUID authorAccountId() {
    return authorAccountId;
  }

  public UUID actorAccountId() {
    return actorAccountId;
  }

  public Optional<String> actorAccountType() {
    return Optional.ofNullable(actorAccountType);
  }

  public EngagementEventType eventType() {
    return eventType;
  }

  public Optional<String> contentType() {
    return Optional.ofNullable(contentType);
  }

  public Instant occurredAt() {
    return occurredAt;
  }

  public String sourceEventId() {
    return sourceEventId;
  }

  public Instant createdAt() {
    return createdAt;
  }
}
