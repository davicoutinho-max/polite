package dev.civicpulse.messaging.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.civicpulse.messaging.application.port.out.ConversationParticipantRepository;
import dev.civicpulse.messaging.application.port.out.ConversationRepository;
import dev.civicpulse.messaging.application.port.out.EventPublisher;
import dev.civicpulse.messaging.application.port.out.MessageRepository;
import dev.civicpulse.messaging.domain.exception.ConversationNotFoundException;
import dev.civicpulse.messaging.domain.exception.NotAParticipantException;
import dev.civicpulse.messaging.domain.model.Conversation;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Mock private ConversationRepository conversationRepository;
  @Mock private ConversationParticipantRepository conversationParticipantRepository;
  @Mock private MessageRepository messageRepository;
  @Mock private EventPublisher eventPublisher;

  private MessageService service;

  @BeforeEach
  void setUp() {
    service =
        new MessageService(
            conversationRepository, conversationParticipantRepository, messageRepository, eventPublisher, Clock.fixed(NOW, ZoneOffset.UTC));
  }

  @Test
  void sendThrowsWhenConversationMissing() {
    UUID conversationId = UUID.randomUUID();
    when(conversationRepository.findById(conversationId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.send(conversationId, UUID.randomUUID(), "hi")).isInstanceOf(ConversationNotFoundException.class);
  }

  @Test
  void sendThrowsWhenSenderNotParticipant() {
    UUID conversationId = UUID.randomUUID();
    UUID senderId = UUID.randomUUID();
    Conversation conversation = Conversation.createDirect(conversationId, NOW);
    when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
    when(conversationParticipantRepository.exists(conversationId, senderId)).thenReturn(false);

    assertThatThrownBy(() -> service.send(conversationId, senderId, "hi")).isInstanceOf(NotAParticipantException.class);
  }

  @Test
  void sendSavesMessageUpdatesConversationAndPublishesEvent() {
    UUID conversationId = UUID.randomUUID();
    UUID senderId = UUID.randomUUID();
    Conversation conversation = Conversation.createDirect(conversationId, NOW);
    when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
    when(conversationParticipantRepository.exists(conversationId, senderId)).thenReturn(true);
    when(messageRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    var message = service.send(conversationId, senderId, "hello there");

    assertThat(message.body()).isEqualTo("hello there");
    assertThat(conversation.lastMessageAt()).contains(NOW);
    verify(conversationRepository).save(conversation);
    verify(eventPublisher).publish(any());
  }

  @Test
  void listByConversationDelegatesToRepository() {
    UUID conversationId = UUID.randomUUID();

    service.listByConversation(conversationId, 0, 20);

    verify(messageRepository).findByConversationId(conversationId, 0, 20);
  }
}
