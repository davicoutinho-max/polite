package dev.civicpulse.assistant.adapter.in.web.dto;

import dev.civicpulse.assistant.application.AssistantTopicView;
import java.util.Map;
import java.util.UUID;

public record AssistantTopicResponse(UUID id, String reference, String title, UUID legislativeItemId, Map<String, String> answers) {

  public static AssistantTopicResponse from(AssistantTopicView view) {
    return new AssistantTopicResponse(view.id(), view.reference(), view.title(), view.legislativeItemId(), view.answers());
  }
}
