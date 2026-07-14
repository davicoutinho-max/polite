package dev.civicpulse.assistant.application;

import dev.civicpulse.assistant.domain.model.AssistantAnswer;
import dev.civicpulse.assistant.domain.model.AssistantTopic;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Read-model matching the frontend mock's shape 1:1 — a topic plus its answers keyed by
 * prompt kind (see AssistantTopic.answers in the frontend's assistant.model.ts). */
public record AssistantTopicView(UUID id, String reference, String title, UUID legislativeItemId, Map<String, String> answers) {

  public static AssistantTopicView of(AssistantTopic topic, List<AssistantAnswer> answers) {
    Map<String, String> answerMap = answers.stream().collect(java.util.stream.Collectors.toMap(a -> a.promptKind().code(), AssistantAnswer::answerText));
    return new AssistantTopicView(topic.id().orElse(null), topic.reference(), topic.title(), topic.legislativeItemId().orElse(null), answerMap);
  }
}
