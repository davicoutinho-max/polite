package dev.civicpulse.assistant.adapter.out.persistence;

import dev.civicpulse.assistant.application.port.out.AssistantTopicRepository;
import dev.civicpulse.assistant.domain.model.AssistantTopic;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class AssistantTopicRepositoryAdapter implements AssistantTopicRepository {

  private final AssistantTopicJpaRepository jpaRepository;
  private final AssistantTopicMapper mapper;

  AssistantTopicRepositoryAdapter(AssistantTopicJpaRepository jpaRepository, AssistantTopicMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public AssistantTopic save(AssistantTopic topic) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(topic)));
  }

  @Override
  public Optional<AssistantTopic> findById(UUID id) {
    return jpaRepository.findById(id).map(mapper::toDomain);
  }

  @Override
  public List<AssistantTopic> findAll() {
    return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
  }
}
