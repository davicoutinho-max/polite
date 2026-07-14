package dev.civicpulse.messaging.adapter.out.persistence;

import dev.civicpulse.messaging.application.port.out.ConversationRepository;
import dev.civicpulse.messaging.domain.model.Conversation;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class ConversationRepositoryAdapter implements ConversationRepository {

  private final ConversationJpaRepository jpaRepository;
  private final ConversationParticipantJpaRepository participantJpaRepository;
  private final ConversationMapper mapper;

  ConversationRepositoryAdapter(
      ConversationJpaRepository jpaRepository, ConversationParticipantJpaRepository participantJpaRepository, ConversationMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.participantJpaRepository = participantJpaRepository;
    this.mapper = mapper;
  }

  @Override
  public Conversation save(Conversation conversation) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(conversation)));
  }

  @Override
  public Optional<Conversation> findById(UUID id) {
    return jpaRepository.findById(id).map(mapper::toDomain);
  }

  @Override
  public List<Conversation> findByParticipant(UUID accountId) {
    List<UUID> conversationIds = participantJpaRepository.findByAccountId(accountId).stream().map(ConversationParticipantJpaEntity::getConversationId).toList();
    if (conversationIds.isEmpty()) {
      return List.of();
    }
    return jpaRepository.findByIdInOrdered(conversationIds).stream().map(mapper::toDomain).toList();
  }

  @Override
  public Optional<Conversation> findDirectBetween(UUID accountA, UUID accountB) {
    return jpaRepository.findDirectBetween(accountA, accountB).stream().findFirst().map(mapper::toDomain);
  }
}
