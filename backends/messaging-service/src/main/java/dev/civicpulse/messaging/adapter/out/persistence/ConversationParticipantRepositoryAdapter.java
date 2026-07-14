package dev.civicpulse.messaging.adapter.out.persistence;

import dev.civicpulse.messaging.application.port.out.ConversationParticipantRepository;
import dev.civicpulse.messaging.domain.model.ConversationParticipant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class ConversationParticipantRepositoryAdapter implements ConversationParticipantRepository {

  private final ConversationParticipantJpaRepository jpaRepository;
  private final ConversationParticipantMapper mapper;

  ConversationParticipantRepositoryAdapter(ConversationParticipantJpaRepository jpaRepository, ConversationParticipantMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public ConversationParticipant save(ConversationParticipant participant) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(participant)));
  }

  @Override
  public List<ConversationParticipant> findByConversationId(UUID conversationId) {
    return jpaRepository.findByConversationId(conversationId).stream().map(mapper::toDomain).toList();
  }

  @Override
  public boolean exists(UUID conversationId, UUID accountId) {
    return jpaRepository.existsByConversationIdAndAccountId(conversationId, accountId);
  }
}
