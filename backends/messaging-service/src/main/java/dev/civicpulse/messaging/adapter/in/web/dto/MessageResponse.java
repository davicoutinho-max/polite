package dev.civicpulse.messaging.adapter.in.web.dto;

import dev.civicpulse.messaging.domain.model.Message;
import java.time.Instant;
import java.util.UUID;

public record MessageResponse(
    UUID id,
    UUID conversationId,
    UUID senderAccountId,
    String body,
    Instant createdAt,
    Instant editedAt,
    boolean deleted,
    String attachmentUrl,
    String attachmentType,
    String attachmentFileName,
    UUID replyToMessageId) {

  public static MessageResponse from(Message message) {
    return new MessageResponse(
        message.id(),
        message.conversationId(),
        message.senderAccountId(),
        message.body(),
        message.createdAt(),
        message.editedAt().orElse(null),
        message.isDeleted(),
        message.attachmentUrl().orElse(null),
        message.attachmentType().map(t -> t.code()).orElse(null),
        message.attachmentFileName().orElse(null),
        message.replyToMessageId().orElse(null));
  }
}
