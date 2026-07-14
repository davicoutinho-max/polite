package dev.civicpulse.messaging.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ConversationTest {

  @Test
  void createDirectIsNotGroup() {
    Conversation conversation = Conversation.createDirect(UUID.randomUUID(), Instant.now());

    assertThat(conversation.group()).isFalse();
    assertThat(conversation.groupName()).isEmpty();
    assertThat(conversation.lastMessageAt()).isEmpty();
  }

  @Test
  void createGroupRequiresName() {
    assertThatThrownBy(() -> Conversation.createGroup(UUID.randomUUID(), " ", null, Instant.now())).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void createGroupKeepsFields() {
    Conversation conversation = Conversation.createGroup(UUID.randomUUID(), "Coalition chat", "http://avatar", Instant.now());

    assertThat(conversation.group()).isTrue();
    assertThat(conversation.groupName()).contains("Coalition chat");
    assertThat(conversation.groupAvatarUrl()).contains("http://avatar");
  }

  @Test
  void recordMessageSentUpdatesLastMessageAt() {
    Instant t0 = Instant.parse("2026-01-01T00:00:00Z");
    Instant t1 = Instant.parse("2026-01-01T01:00:00Z");
    Conversation conversation = Conversation.createDirect(UUID.randomUUID(), t0);

    conversation.recordMessageSent(t1);

    assertThat(conversation.lastMessageAt()).contains(t1);
  }
}
