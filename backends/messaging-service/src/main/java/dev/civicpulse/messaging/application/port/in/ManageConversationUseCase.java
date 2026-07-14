package dev.civicpulse.messaging.application.port.in;

import dev.civicpulse.messaging.domain.model.Conversation;
import java.util.List;
import java.util.UUID;

public interface ManageConversationUseCase {

  Conversation startDirect(UUID accountA, UUID accountB);

  Conversation startGroup(List<UUID> participantAccountIds, String groupName, String groupAvatarUrl);

  void markRead(UUID conversationId, UUID accountId);
}
