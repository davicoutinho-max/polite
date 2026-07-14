package dev.civicpulse.livestreaming.application.port.out;

import dev.civicpulse.livestreaming.domain.model.LiveChatMessage;
import java.util.List;
import java.util.UUID;

public interface LiveChatMessageRepository {

  LiveChatMessage save(LiveChatMessage message);

  List<LiveChatMessage> findBySessionId(UUID liveSessionId);

  long countBySessionId(UUID liveSessionId);
}
