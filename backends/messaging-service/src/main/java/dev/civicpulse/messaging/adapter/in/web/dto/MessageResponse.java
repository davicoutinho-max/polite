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
    boolean deleted) {

  public static MessageResponse from(Message message) {
    return new MessageResponse(
        message.id(),
        message.conversationId(),
        message.senderAccountId(),
        message.body(),
        message.createdAt(),
        message.editedAt().orElse(null),
        message.isDeleted());
  }
}
