package dev.civicpulse.messaging.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Maps only {@code id} as the JPA identifier even though the physical primary key is the
 * composite {@code (id, conversation_id)} (required by Postgres for hash-partitioned tables) —
 * see feed-content-service's {@code PostJpaEntity} for the identical rationale. */
@Entity
@Table(name = "messages")
public class MessageJpaEntity {

  @Id private UUID id;

  @Column(name = "conversation_id", nullable = false)
  private UUID conversationId;

  @Column(name = "sender_account_id", nullable = false)
  private UUID senderAccountId;

  @Column
  private String body;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "edited_at")
  private Instant editedAt;

  @Column(name = "deleted_at")
  private Instant deletedAt;

  protected MessageJpaEntity() {}

  public MessageJpaEntity(
      UUID id, UUID conversationId, UUID senderAccountId, String body, Instant createdAt, Instant editedAt, Instant deletedAt) {
    this.id = id;
    this.conversationId = conversationId;
    this.senderAccountId = senderAccountId;
    this.body = body;
    this.createdAt = createdAt;
    this.editedAt = editedAt;
    this.deletedAt = deletedAt;
  }

  public UUID getId() {
    return id;
  }

  public UUID getConversationId() {
    return conversationId;
  }

  public UUID getSenderAccountId() {
    return senderAccountId;
  }

  public String getBody() {
    return body;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getEditedAt() {
    return editedAt;
  }

  public Instant getDeletedAt() {
    return deletedAt;
  }
}
