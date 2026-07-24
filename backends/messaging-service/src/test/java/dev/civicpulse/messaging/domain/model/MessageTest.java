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

  @Test
  void sendWithAttachmentAllowsBlankBody() {
    Message message =
        Message.sendWithAttachment(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            null,
            "https://cdn.example.com/clip.webm",
            AttachmentType.AUDIO,
            "clip.webm",
            null,
            Instant.now());

    assertThat(message.body()).isNull();
    assertThat(message.attachmentUrl()).contains("https://cdn.example.com/clip.webm");
    assertThat(message.attachmentType()).contains(AttachmentType.AUDIO);
    assertThat(message.attachmentFileName()).contains("clip.webm");
  }

  @Test
  void sendWithAttachmentRequiresAttachmentUrl() {
    assertThatThrownBy(
            () ->
                Message.sendWithAttachment(
                    UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), null, null, AttachmentType.AUDIO, "clip.webm", null, Instant.now()))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void sendReplyKeepsReplyToMessageId() {
    UUID replyToId = UUID.randomUUID();

    Message message = Message.sendReply(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "quoting you", replyToId, Instant.now());

    assertThat(message.replyToMessageId()).contains(replyToId);
  }
}
