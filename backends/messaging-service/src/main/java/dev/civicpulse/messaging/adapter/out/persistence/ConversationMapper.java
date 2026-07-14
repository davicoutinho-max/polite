package dev.civicpulse.messaging.adapter.out.persistence;

import dev.civicpulse.messaging.domain.model.Conversation;
import org.springframework.stereotype.Component;

@Component
class ConversationMapper {

  Conversation toDomain(ConversationJpaEntity entity) {
    return Conversation.reconstitute(
        entity.getId(), entity.isGroup(), entity.getGroupName(), entity.getGroupAvatarUrl(), entity.getCreatedAt(), entity.getLastMessageAt());
  }

  ConversationJpaEntity toEntity(Conversation conversation) {
    return new ConversationJpaEntity(
        conversation.id(), conversation.group(), conversation.groupName().orElse(null), conversation.groupAvatarUrl().orElse(null),
        conversation.createdAt(), conversation.lastMessageAt().orElse(null));
  }
}
