package dev.civicpulse.livestreaming.adapter.out.persistence;

import dev.civicpulse.livestreaming.domain.model.LiveChatMessage;
import org.springframework.stereotype.Component;

@Component
class LiveChatMessageMapper {

  LiveChatMessage toDomain(LiveChatMessageJpaEntity entity) {
    return LiveChatMessage.reconstitute(entity.getId(), entity.getLiveSessionId(), entity.getAccountId(), entity.getBody(), entity.getSentAt());
  }

  LiveChatMessageJpaEntity toEntity(LiveChatMessage message) {
    return new LiveChatMessageJpaEntity(
        message.id().orElse(null), message.liveSessionId(), message.accountId(), message.body(), message.sentAt());
  }
}
