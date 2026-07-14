package dev.civicpulse.messaging.adapter.in.web.dto;

import dev.civicpulse.messaging.domain.model.ConversationParticipant;
import java.time.Instant;
import java.util.UUID;

public record ParticipantResponse(UUID conversationId, UUID accountId, Instant joinedAt, Instant lastReadAt) {

  public static ParticipantResponse from(ConversationParticipant participant) {
    return new ParticipantResponse(
        participant.conversationId(), participant.accountId(), participant.joinedAt(), participant.lastReadAt().orElse(null));
  }
}
