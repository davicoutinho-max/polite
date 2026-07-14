package dev.civicpulse.messaging.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class Message {

  private final UUID id;
  private final UUID conversationId;
  private final UUID senderAccountId;
  private final String body;
  private final Instant createdAt;

  private Message(UUID id, UUID conversationId, UUID senderAccountId, String body, Instant createdAt) {
    this.id = Objects.requireNonNull(id);
    this.conversationId = Objects.requireNonNull(conversationId);
    this.senderAccountId = Objects.requireNonNull(senderAccountId);
    this.body = requireNonBlank(body);
    this.createdAt = Objects.requireNonNull(createdAt);
  }

  public static Message send(UUID id, UUID conversationId, UUID senderAccountId, String body, Instant now) {
    return new Message(id, conversationId, senderAccountId, body, now);
  }

  public static Message reconstitute(UUID id, UUID conversationId, UUID senderAccountId, String body, Instant createdAt) {
    return new Message(id, conversationId, senderAccountId, body, createdAt);
  }

  private static String requireNonBlank(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("body must not be blank");
    }
    return value;
  }

  public UUID id() {
    return id;
  }

  public UUID conversationId() {
    return conversationId;
  }

  public UUID senderAccountId() {
    return senderAccountId;
  }

  public String body() {
    return body;
  }

  public Instant createdAt() {
    return createdAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Message other)) return false;
    return id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
