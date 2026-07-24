package dev.civicpulse.messaging.adapter.out.persistence;

import dev.civicpulse.messaging.domain.model.AttachmentType;
import dev.civicpulse.messaging.domain.model.Message;
import org.springframework.stereotype.Component;

@Component
class MessageMapper {

  Message toDomain(MessageJpaEntity entity) {
    return Message.reconstitute(
        entity.getId(),
        entity.getConversationId(),
        entity.getSenderAccountId(),
        entity.getBody(),
        entity.getCreatedAt(),
        entity.getEditedAt(),
        entity.getDeletedAt(),
        entity.getAttachmentUrl(),
        entity.getAttachmentType() != null ? AttachmentType.fromCode(entity.getAttachmentType()) : null,
        entity.getAttachmentFileName(),
        entity.getReplyToMessageId());
  }

  MessageJpaEntity toEntity(Message message) {
    return new MessageJpaEntity(
        message.id(),
        message.conversationId(),
        message.senderAccountId(),
        message.body(),
        message.createdAt(),
        message.editedAt().orElse(null),
        message.deletedAt().orElse(null),
        message.attachmentUrl().orElse(null),
        message.attachmentType().map(AttachmentType::code).orElse(null),
        message.attachmentFileName().orElse(null),
        message.replyToMessageId().orElse(null));
  }
}
