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

  @Column(name = "attachment_url")
  private String attachmentUrl;

  @Column(name = "attachment_type")
  private String attachmentType;

  @Column(name = "attachment_file_name")
  private String attachmentFileName;

  @Column(name = "reply_to_message_id")
  private UUID replyToMessageId;

  protected MessageJpaEntity() {}

  public MessageJpaEntity(
      UUID id,
      UUID conversationId,
      UUID senderAccountId,
      String body,
      Instant createdAt,
      Instant editedAt,
      Instant deletedAt,
      String attachmentUrl,
      String attachmentType,
      String attachmentFileName,
      UUID replyToMessageId) {
    this.id = id;
    this.conversationId = conversationId;
    this.senderAccountId = senderAccountId;
    this.body = body;
    this.createdAt = createdAt;
    this.editedAt = editedAt;
    this.deletedAt = deletedAt;
    this.attachmentUrl = attachmentUrl;
    this.attachmentType = attachmentType;
    this.attachmentFileName = attachmentFileName;
    this.replyToMessageId = replyToMessageId;
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

  public String getAttachmentUrl() {
    return attachmentUrl;
  }

  public String getAttachmentType() {
    return attachmentType;
  }

  public String getAttachmentFileName() {
    return attachmentFileName;
  }

  public UUID getReplyToMessageId() {
    return replyToMessageId;
  }
}
