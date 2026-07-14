package dev.civicpulse.messaging.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface ConversationParticipantJpaRepository extends JpaRepository<ConversationParticipantJpaEntity, ConversationParticipantId> {

  List<ConversationParticipantJpaEntity> findByConversationId(UUID conversationId);

  List<ConversationParticipantJpaEntity> findByAccountId(UUID accountId);

  boolean existsByConversationIdAndAccountId(UUID conversationId, UUID accountId);
}
