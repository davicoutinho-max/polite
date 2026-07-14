package dev.civicpulse.livestreaming.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.civicpulse.livestreaming.application.port.out.LiveChatMessageRepository;
import dev.civicpulse.livestreaming.domain.model.LiveChatMessage;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChatArchiveServiceTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Mock private LiveChatMessageRepository liveChatMessageRepository;

  private ChatArchiveService service;

  @BeforeEach
  void setUp() {
    service = new ChatArchiveService(liveChatMessageRepository, Clock.fixed(NOW, ZoneOffset.UTC));
  }

  @Test
  void archiveMessageSavesWithCurrentTimestamp() {
    when(liveChatMessageRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    UUID sessionId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();

    LiveChatMessage message = service.archiveMessage(sessionId, accountId, "hello chat");

    assertThat(message.liveSessionId()).isEqualTo(sessionId);
    assertThat(message.accountId()).isEqualTo(accountId);
    assertThat(message.sentAt()).isEqualTo(NOW);
  }

  @Test
  void listArchivedMessagesDelegatesToRepository() {
    UUID sessionId = UUID.randomUUID();

    service.listArchivedMessages(sessionId);

    verify(liveChatMessageRepository).findBySessionId(sessionId);
  }
}
