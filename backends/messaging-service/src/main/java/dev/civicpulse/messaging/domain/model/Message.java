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
  private final String attachmentUrl;
  private final AttachmentType attachmentType;
  private final String attachmentFileName;
  private final UUID replyToMessageId;

  private Message(
      UUID id,
      UUID conversationId,
      UUID senderAccountId,
      String body,
      Instant createdAt,
      Instant editedAt,
      Instant deletedAt,
      String attachmentUrl,
      AttachmentType attachmentType,
      String attachmentFileName,
      UUID replyToMessageId) {
    this.id = Objects.requireNonNull(id);
    this.conversationId = Objects.requireNonNull(conversationId);
    this.senderAccountId = Objects.requireNonNull(senderAccountId);
    this.attachmentUrl = attachmentUrl;
    this.attachmentType = attachmentType;
    this.attachmentFileName = attachmentFileName;
    this.replyToMessageId = replyToMessageId;
    this.body = deletedAt == null ? requireBodyOrAttachment(body, attachmentUrl) : body;
    this.createdAt = Objects.requireNonNull(createdAt);
    this.editedAt = editedAt;
    this.deletedAt = deletedAt;
  }

  public static Message send(UUID id, UUID conversationId, UUID senderAccountId, String body, Instant now) {
    return new Message(id, conversationId, senderAccountId, body, now, null, null, null, null, null, null);
  }

  /** A message carrying an audio/video/file/image attachment — {@code body} may be blank (a bare
   * attachment with no caption), but the attachment fields themselves are required together. */
  public static Message sendWithAttachment(
      UUID id,
      UUID conversationId,
      UUID senderAccountId,
      String body,
      String attachmentUrl,
      AttachmentType attachmentType,
      String attachmentFileName,
      UUID replyToMessageId,
      Instant now) {
    Objects.requireNonNull(attachmentUrl, "attachmentUrl must not be null");
    Objects.requireNonNull(attachmentType, "attachmentType must not be null");
    return new Message(
        id, conversationId, senderAccountId, body, now, null, null, attachmentUrl, attachmentType, attachmentFileName, replyToMessageId);
  }

  /** Plain text message that quotes an earlier message in the same conversation — the target's
   * existence/membership isn't re-validated here (it's a soft, display-only reference, same
   * relationship strength as every other cross-aggregate id in this system). */
  public static Message sendReply(
      UUID id, UUID conversationId, UUID senderAccountId, String body, UUID replyToMessageId, Instant now) {
    return new Message(id, conversationId, senderAccountId, body, now, null, null, null, null, null, replyToMessageId);
  }

  public static Message reconstitute(
      UUID id,
      UUID conversationId,
      UUID senderAccountId,
      String body,
      Instant createdAt,
      Instant editedAt,
      Instant deletedAt,
      String attachmentUrl,
      AttachmentType attachmentType,
      String attachmentFileName,
      UUID replyToMessageId) {
    return new Message(
        id,
        conversationId,
        senderAccountId,
        body,
        createdAt,
        editedAt,
        deletedAt,
        attachmentUrl,
        attachmentType,
        attachmentFileName,
        replyToMessageId);
  }

  /** Only the sender edits their own message (enforced by the caller — see MessageService).
   * Text-only by design: a message carrying an audio/video/file/image attachment can't have its
   * caption edited (delete-and-resend is the only path for those), so there's no ambiguity in
   * the UI about what "edited" means for an attachment. */
  public void edit(String newBody, Instant now) {
    if (deletedAt != null) {
      throw new IllegalStateException("Cannot edit a deleted message");
    }
    if (attachmentUrl != null) {
      throw new IllegalStateException("Cannot edit a message that carries an attachment");
    }
    this.body = requireBodyOrAttachment(newBody, null);
    this.editedAt = now;
  }

  /** Soft delete: the body is cleared (not merely hidden) so the original content doesn't linger
   * in the database once "deleted" — the frontend renders a tombstone ("This message was
   * deleted") for any message with `deletedAt` set instead of showing `body`. The attachment URL
   * itself is left as-is (deleting the file it points to is out of scope here, same as the rest
   * of this pass's soft-delete model). */
  public void delete(Instant now) {
    if (deletedAt != null) {
      return;
    }
    this.body = null;
    this.deletedAt = now;
  }

  private static String requireBodyOrAttachment(String body, String attachmentUrl) {
    if ((body == null || body.isBlank()) && attachmentUrl == null) {
      throw new IllegalArgumentException("body must not be blank unless the message carries an attachment");
    }
    return body;
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

  public Optional<String> attachmentUrl() {
    return Optional.ofNullable(attachmentUrl);
  }

  public Optional<AttachmentType> attachmentType() {
    return Optional.ofNullable(attachmentType);
  }

  public Optional<String> attachmentFileName() {
    return Optional.ofNullable(attachmentFileName);
  }

  public Optional<UUID> replyToMessageId() {
    return Optional.ofNullable(replyToMessageId);
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
