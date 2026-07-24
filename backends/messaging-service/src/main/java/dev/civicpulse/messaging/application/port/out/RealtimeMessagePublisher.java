package dev.civicpulse.messaging.application.port.out;

import java.time.Instant;
import java.util.UUID;

/** Fan-out to every open tab subscribed to a conversation — separate from {@link EventPublisher}
 * (Kafka), which is for cross-service integration, not "update this browser tab right now". */
public interface RealtimeMessagePublisher {

  /** Covers send, edit and delete alike — {@code body} is null and {@code deleted} is true for a
   * delete, mirroring MessageResponse's own shape. Attachment fields are null for a plain text
   * message. */
  void messageUpdated(
      UUID conversationId,
      UUID messageId,
      UUID senderAccountId,
      String body,
      Instant createdAt,
      Instant editedAt,
      boolean deleted,
      String attachmentUrl,
      String attachmentType,
      String attachmentFileName,
      UUID replyToMessageId);

  void conversationRead(UUID conversationId, UUID accountId, Instant readAt);
}
