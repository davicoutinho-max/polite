package dev.civicpulse.messaging.adapter.out.persistence;

import dev.civicpulse.messaging.domain.model.ConversationParticipant;
import org.springframework.stereotype.Component;

@Component
class ConversationParticipantMapper {

  ConversationParticipant toDomain(ConversationParticipantJpaEntity entity) {
    return ConversationParticipant.reconstitute(entity.getConversationId(), entity.getAccountId(), entity.getJoinedAt(), entity.getLastReadAt());
  }

  ConversationParticipantJpaEntity toEntity(ConversationParticipant participant) {
    return new ConversationParticipantJpaEntity(
        participant.conversationId(), participant.accountId(), participant.joinedAt(), participant.lastReadAt().orElse(null));
  }
}
