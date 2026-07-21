package dev.civicpulse.messaging.application.port.out;

import dev.civicpulse.messaging.domain.model.Conversation;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository {

  Conversation save(Conversation conversation);

  Optional<Conversation> findById(UUID id);

  /** Reverse-chronological by {@code last_message_at} — the conversation list's natural sort. */
  List<Conversation> findByParticipant(UUID accountId);

  /** Finds an existing non-group conversation between exactly these two accounts, if any —
   * direct conversations are reused rather than duplicated. */
  Optional<Conversation> findDirectBetween(UUID accountA, UUID accountB);

  void deleteById(UUID id);
}
