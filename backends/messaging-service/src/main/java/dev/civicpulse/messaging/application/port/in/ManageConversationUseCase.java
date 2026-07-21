package dev.civicpulse.messaging.application.port.in;

import dev.civicpulse.messaging.domain.model.Conversation;
import java.util.List;
import java.util.UUID;

public interface ManageConversationUseCase {

  Conversation startDirect(UUID accountA, UUID accountB);

  Conversation startGroup(List<UUID> participantAccountIds, String groupName, String groupAvatarUrl);

  void markRead(UUID conversationId, UUID accountId);

  /** Any participant may rename the group — there's no separate "admin" role in this model (see
   * MessagingServiceApplication's scope note on group membership). */
  Conversation renameGroup(UUID conversationId, UUID requesterAccountId, String newName);

  Conversation changeGroupAvatar(UUID conversationId, UUID requesterAccountId, String newAvatarUrl);
}
