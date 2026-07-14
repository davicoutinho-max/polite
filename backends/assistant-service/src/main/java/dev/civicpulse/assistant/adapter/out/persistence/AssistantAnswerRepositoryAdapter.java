package dev.civicpulse.assistant.adapter.out.persistence;

import dev.civicpulse.assistant.application.port.out.AssistantAnswerRepository;
import dev.civicpulse.assistant.domain.model.AssistantAnswer;
import dev.civicpulse.assistant.domain.model.AssistantPromptKind;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class AssistantAnswerRepositoryAdapter implements AssistantAnswerRepository {

  private final AssistantAnswerJpaRepository jpaRepository;
  private final AssistantAnswerMapper mapper;

  AssistantAnswerRepositoryAdapter(AssistantAnswerJpaRepository jpaRepository, AssistantAnswerMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public AssistantAnswer save(AssistantAnswer answer) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(answer)));
  }

  @Override
  public List<AssistantAnswer> findByTopicId(UUID topicId) {
    return jpaRepository.findByTopicId(topicId).stream().map(mapper::toDomain).toList();
  }

  @Override
  public Optional<AssistantAnswer> findByTopicIdAndPromptKind(UUID topicId, AssistantPromptKind promptKind) {
    return jpaRepository.findByTopicIdAndPromptKind(topicId, promptKind).map(mapper::toDomain);
  }
}
