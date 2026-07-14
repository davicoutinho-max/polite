package dev.civicpulse.assistant.application.port.out;

import dev.civicpulse.assistant.domain.model.AssistantAnswer;
import dev.civicpulse.assistant.domain.model.AssistantPromptKind;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssistantAnswerRepository {

  AssistantAnswer save(AssistantAnswer answer);

  List<AssistantAnswer> findByTopicId(UUID topicId);

  Optional<AssistantAnswer> findByTopicIdAndPromptKind(UUID topicId, AssistantPromptKind promptKind);
}
