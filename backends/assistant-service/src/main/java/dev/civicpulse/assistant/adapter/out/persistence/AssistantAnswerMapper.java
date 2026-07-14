package dev.civicpulse.assistant.adapter.out.persistence;

import dev.civicpulse.assistant.domain.model.AssistantAnswer;
import org.springframework.stereotype.Component;

@Component
class AssistantAnswerMapper {

  AssistantAnswer toDomain(AssistantAnswerJpaEntity entity) {
    return AssistantAnswer.reconstitute(entity.getId(), entity.getTopicId(), entity.getPromptKind(), entity.getAnswerText());
  }

  AssistantAnswerJpaEntity toEntity(AssistantAnswer answer) {
    return new AssistantAnswerJpaEntity(answer.id().orElse(null), answer.topicId(), answer.promptKind(), answer.answerText());
  }
}
