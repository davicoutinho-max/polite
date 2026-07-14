package dev.civicpulse.messaging.application.port.out;

import dev.civicpulse.messaging.domain.model.ConversationParticipant;
import java.util.List;
import java.util.UUID;

public interface ConversationParticipantRepository {

  ConversationParticipant save(ConversationParticipant participant);

  List<ConversationParticipant> findByConversationId(UUID conversationId);

  boolean exists(UUID conversationId, UUID accountId);
}
