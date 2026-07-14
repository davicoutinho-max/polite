package dev.civicpulse.livestreaming.application;

import dev.civicpulse.livestreaming.application.port.in.ArchiveChatUseCase;
import dev.civicpulse.livestreaming.application.port.out.LiveChatMessageRepository;
import dev.civicpulse.livestreaming.domain.model.LiveChatMessage;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatArchiveService implements ArchiveChatUseCase {

  private final LiveChatMessageRepository liveChatMessageRepository;
  private final Clock clock;

  public ChatArchiveService(LiveChatMessageRepository liveChatMessageRepository, Clock clock) {
    this.liveChatMessageRepository = liveChatMessageRepository;
    this.clock = clock;
  }

  @Override
  @Transactional
  public LiveChatMessage archiveMessage(UUID sessionId, UUID accountId, String body) {
    return liveChatMessageRepository.save(LiveChatMessage.archive(sessionId, accountId, body, clock.instant()));
  }

  @Override
  @Transactional(readOnly = true)
  public List<LiveChatMessage> listArchivedMessages(UUID sessionId) {
    return liveChatMessageRepository.findBySessionId(sessionId);
  }
}
