package dev.civicpulse.messaging.application.port.in;

import dev.civicpulse.messaging.domain.model.Conversation;
import dev.civicpulse.messaging.domain.model.ConversationParticipant;
import java.util.List;
import java.util.UUID;

public interface GetConversationUseCase {

  Conversation getById(UUID id);

  List<Conversation> listByParticipant(UUID accountId);

  List<ConversationParticipant> listParticipants(UUID conversationId);
}
