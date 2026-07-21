package dev.civicpulse.messaging.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class Message {

  private final UUID id;
  private final UUID conversationId;
  private final UUID senderAccountId;
  private String body;
  private final Instant createdAt;
  private Instant editedAt;
  private Instant deletedAt;

  private Message(
      UUID id, UUID conversationId, UUID senderAccountId, String body, Instant createdAt, Instant editedAt, Instant deletedAt) {
    this.id = Objects.requireNonNull(id);
    this.conversationId = Objects.requireNonNull(conversationId);
    this.senderAccountId = Objects.requireNonNull(senderAccountId);
    this.body = deletedAt == null ? requireNonBlank(body) : body;
    this.createdAt = Objects.requireNonNull(createdAt);
    this.editedAt = editedAt;
    this.deletedAt = deletedAt;
  }

  public static Message send(UUID id, UUID conversationId, UUID senderAccountId, String body, Instant now) {
    return new Message(id, conversationId, senderAccountId, body, now, null, null);
  }

  public static Message reconstitute(
      UUID id, UUID conversationId, UUID senderAccountId, String body, Instant createdAt, Instant editedAt, Instant deletedAt) {
    return new Message(id, conversationId, senderAccountId, body, createdAt, editedAt, deletedAt);
  }

  /** Only the sender edits their own message (enforced by the caller — see MessageService) — this
   * just refuses to resurrect a deleted message with new content. */
  public void edit(String newBody, Instant now) {
    if (deletedAt != null) {
      throw new IllegalStateException("Cannot edit a deleted message");
    }
    this.body = requireNonBlank(newBody);
    this.editedAt = now;
  }

  /** Soft delete: the body is cleared (not merely hidden) so the original content doesn't linger
   * in the database once "deleted" — the frontend renders a tombstone ("This message was
   * deleted") for any message with `deletedAt` set instead of showing `body`. */
  public void delete(Instant now) {
    if (deletedAt != null) {
      return;
    }
    this.body = null;
    this.deletedAt = now;
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

  /** Null once deleted — see {@link #delete(Instant)}. */
  public String body() {
    return body;
  }

  public Instant createdAt() {
    return createdAt;
  }

  public Optional<Instant> editedAt() {
    return Optional.ofNullable(editedAt);
  }

  public Optional<Instant> deletedAt() {
    return Optional.ofNullable(deletedAt);
  }

  public boolean isDeleted() {
    return deletedAt != null;
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
