package dev.civicpulse.messaging.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ConversationParticipantTest {

  @Test
  void joinStartsWithNoLastRead() {
    ConversationParticipant participant = ConversationParticipant.join(UUID.randomUUID(), UUID.randomUUID(), Instant.now());

    assertThat(participant.lastReadAt()).isEmpty();
  }

  @Test
  void markReadSetsTimestamp() {
    Instant now = Instant.parse("2026-01-01T00:00:00Z");
    ConversationParticipant participant = ConversationParticipant.join(UUID.randomUUID(), UUID.randomUUID(), Instant.now());

    participant.markRead(now);

    assertThat(participant.lastReadAt()).contains(now);
  }
}
