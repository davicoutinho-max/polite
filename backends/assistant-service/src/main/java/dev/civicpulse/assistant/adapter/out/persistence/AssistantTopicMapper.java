package dev.civicpulse.assistant.adapter.out.persistence;

import dev.civicpulse.assistant.domain.model.AssistantTopic;
import org.springframework.stereotype.Component;

@Component
class AssistantTopicMapper {

  AssistantTopic toDomain(AssistantTopicJpaEntity entity) {
    return AssistantTopic.reconstitute(entity.getId(), entity.getReference(), entity.getTitle(), entity.getLegislativeItemId(), entity.getCreatedAt());
  }

  AssistantTopicJpaEntity toEntity(AssistantTopic topic) {
    return new AssistantTopicJpaEntity(
        topic.id().orElse(null), topic.reference(), topic.title(), topic.legislativeItemId().orElse(null), topic.createdAt());
  }
}
