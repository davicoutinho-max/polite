package dev.civicpulse.assistant.application.port.in;

import dev.civicpulse.assistant.application.AssistantTopicView;
import dev.civicpulse.assistant.domain.model.AssistantPromptKind;
import java.util.List;
import java.util.UUID;

public interface AssistantUseCase {

  List<AssistantTopicView> listTopics();

  AssistantTopicView getTopic(UUID id);

  AssistantTopicView createTopic(String reference, String title, UUID legislativeItemId);

  AssistantTopicView writeAnswer(UUID topicId, AssistantPromptKind promptKind, String answerText);
}
