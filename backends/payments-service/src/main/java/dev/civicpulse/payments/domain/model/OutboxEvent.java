package dev.civicpulse.payments.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Transactional outbox — written in the same DB transaction as the {@code payment_intents}
 * row it describes, published by a separate relay process (see application.OutboxRelayService).
 * Guarantees no "charged but event lost" gap: the DB commit and the "this will eventually reach
 * Kafka" guarantee are the same atomic operation. */
public final class OutboxEvent {

  private final UUID id;
  private final String aggregateType;
  private final UUID aggregateId;
  private final String eventType;
  private final String payload;
  private final Instant createdAt;
  private Instant publishedAt;

  private OutboxEvent(UUID id, String aggregateType, UUID aggregateId, String eventType, String payload, Instant createdAt, Instant publishedAt) {
    this.id = Objects.requireNonNull(id);
    this.aggregateType = requireNonBlank(aggregateType);
    this.aggregateId = Objects.requireNonNull(aggregateId);
    this.eventType = requireNonBlank(eventType);
    this.payload = requireNonBlank(payload);
    this.createdAt = Objects.requireNonNull(createdAt);
    this.publishedAt = publishedAt;
  }

  public static OutboxEvent record(UUID id, UUID aggregateId, String eventType, String payload, Instant now) {
    return new OutboxEvent(id, "payment_intent", aggregateId, eventType, payload, now, null);
  }

  public static OutboxEvent reconstitute(
      UUID id, String aggregateType, UUID aggregateId, String eventType, String payload, Instant createdAt, Instant publishedAt) {
    return new OutboxEvent(id, aggregateType, aggregateId, eventType, payload, createdAt, publishedAt);
  }

  public void markPublished(Instant now) {
    this.publishedAt = now;
  }

  private static String requireNonBlank(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("value must not be blank");
    }
    return value;
  }

  public UUID id() {
    return id;
  }

  public String aggregateType() {
    return aggregateType;
  }

  public UUID aggregateId() {
    return aggregateId;
  }

  public String eventType() {
    return eventType;
  }

  public String payload() {
    return payload;
  }

  public Instant createdAt() {
    return createdAt;
  }

  public Optional<Instant> publishedAt() {
    return Optional.ofNullable(publishedAt);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof OutboxEvent other)) return false;
    return id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
