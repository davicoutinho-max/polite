package dev.civicpulse.messaging.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MessageTest {

  @Test
  void sendKeepsFields() {
    UUID conversationId = UUID.randomUUID();
    UUID senderId = UUID.randomUUID();

    Message message = Message.send(UUID.randomUUID(), conversationId, senderId, "hello", Instant.now());

    assertThat(message.conversationId()).isEqualTo(conversationId);
    assertThat(message.senderAccountId()).isEqualTo(senderId);
    assertThat(message.body()).isEqualTo("hello");
  }

  @Test
  void sendRejectsBlankBody() {
    assertThatThrownBy(() -> Message.send(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), " ", Instant.now()))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
