package dev.civicpulse.livestreaming.application.port.in;

import dev.civicpulse.livestreaming.domain.model.LiveChatMessage;
import java.util.List;
import java.util.UUID;

public interface ArchiveChatUseCase {

  LiveChatMessage archiveMessage(UUID sessionId, UUID accountId, String body);

  List<LiveChatMessage> listArchivedMessages(UUID sessionId);
}
