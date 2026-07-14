package dev.civicpulse.messaging.adapter.in.web.dto;

import dev.civicpulse.messaging.domain.model.Conversation;
import java.time.Instant;
import java.util.UUID;

public record ConversationResponse(
    UUID id, boolean group, String groupName, String groupAvatarUrl, Instant createdAt, Instant lastMessageAt) {

  public static ConversationResponse from(Conversation conversation) {
    return new ConversationResponse(
        conversation.id(), conversation.group(), conversation.groupName().orElse(null), conversation.groupAvatarUrl().orElse(null),
        conversation.createdAt(), conversation.lastMessageAt().orElse(null));
  }
}
