package dev.civicpulse.messaging.adapter.out.persistence;

import dev.civicpulse.messaging.domain.model.Message;
import org.springframework.stereotype.Component;

@Component
class MessageMapper {

  Message toDomain(MessageJpaEntity entity) {
    return Message.reconstitute(entity.getId(), entity.getConversationId(), entity.getSenderAccountId(), entity.getBody(), entity.getCreatedAt());
  }

  MessageJpaEntity toEntity(Message message) {
    return new MessageJpaEntity(message.id(), message.conversationId(), message.senderAccountId(), message.body(), message.createdAt());
  }
}
