package dev.civicpulse.livestreaming.adapter.in.web.dto;

import dev.civicpulse.livestreaming.domain.model.LiveChatMessage;
import java.time.Instant;
import java.util.UUID;

public record ChatMessageResponse(Long id, UUID liveSessionId, UUID accountId, String body, Instant sentAt) {

  public static ChatMessageResponse from(LiveChatMessage message) {
    return new ChatMessageResponse(
        message.id().orElse(null), message.liveSessionId(), message.accountId(), message.body(), message.sentAt());
  }
}
