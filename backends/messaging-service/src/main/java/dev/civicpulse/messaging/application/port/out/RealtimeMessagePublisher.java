package dev.civicpulse.messaging.application.port.out;

import java.time.Instant;
import java.util.UUID;

/** Fan-out to every open tab subscribed to a conversation — separate from {@link EventPublisher}
 * (Kafka), which is for cross-service integration, not "update this browser tab right now". */
public interface RealtimeMessagePublisher {

  void messageSent(UUID conversationId, UUID messageId, UUID senderAccountId, String body, Instant createdAt);

  /** Also used for deletes — {@code body} is null and {@code deleted} is true in that case,
   * mirroring MessageResponse's own shape. */
  void messageUpdated(
      UUID conversationId, UUID messageId, UUID senderAccountId, String body, Instant createdAt, Instant editedAt, boolean deleted);

  void conversationRead(UUID conversationId, UUID accountId, Instant readAt);
}
