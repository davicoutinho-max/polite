package dev.civicpulse.assistant.adapter.out.persistence;

import dev.civicpulse.assistant.domain.model.AssistantPromptKind;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface AssistantAnswerJpaRepository extends JpaRepository<AssistantAnswerJpaEntity, Long> {

  List<AssistantAnswerJpaEntity> findByTopicId(UUID topicId);

  Optional<AssistantAnswerJpaEntity> findByTopicIdAndPromptKind(UUID topicId, AssistantPromptKind promptKind);
}
