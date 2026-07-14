package dev.civicpulse.assistant.application.port.out;

import dev.civicpulse.assistant.domain.model.AssistantTopic;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssistantTopicRepository {

  AssistantTopic save(AssistantTopic topic);

  Optional<AssistantTopic> findById(UUID id);

  List<AssistantTopic> findAll();
}
