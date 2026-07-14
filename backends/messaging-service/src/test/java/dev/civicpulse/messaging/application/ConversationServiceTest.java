package dev.civicpulse.messaging.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.civicpulse.messaging.application.port.out.ConversationParticipantRepository;
import dev.civicpulse.messaging.application.port.out.ConversationRepository;
import dev.civicpulse.messaging.application.port.out.EventPublisher;
import dev.civicpulse.messaging.domain.event.ConversationCreated;
import dev.civicpulse.messaging.domain.model.Conversation;
import dev.civicpulse.messaging.domain.model.ConversationParticipant;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConversationServiceTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Mock private ConversationRepository conversationRepository;
  @Mock private ConversationParticipantRepository conversationParticipantRepository;
  @Mock private EventPublisher eventPublisher;

  private ConversationService service;

  @BeforeEach
  void setUp() {
    service = new ConversationService(conversationRepository, conversationParticipantRepository, eventPublisher, Clock.fixed(NOW, ZoneOffset.UTC));
  }

  @Test
  void startDirectReusesExistingConversation() {
    UUID a = UUID.randomUUID();
    UUID b = UUID.randomUUID();
    Conversation existing = Conversation.createDirect(UUID.randomUUID(), NOW);
    when(conversationRepository.findDirectBetween(a, b)).thenReturn(Optional.of(existing));

    Conversation result = service.startDirect(a, b);

    assertThat(result).isEqualTo(existing);
    verify(conversationRepository, never()).save(any());
  }

  @Test
  void startDirectCreatesNewConversationAndPublishesEvent() {
    UUID a = UUID.randomUUID();
    UUID b = UUID.randomUUID();
    when(conversationRepository.findDirectBetween(a, b)).thenReturn(Optional.empty());
    when(conversationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    Conversation result = service.startDirect(a, b);

    assertThat(result.group()).isFalse();
    verify(conversationParticipantRepository, times(2)).save(any());
    ArgumentCaptor<ConversationCreated> captor = ArgumentCaptor.forClass(ConversationCreated.class);
    verify(eventPublisher).publish(captor.capture());
    assertThat(captor.getValue().group()).isFalse();
    assertThat(captor.getValue().participantAccountIds()).containsExactly(a, b);
  }

  @Test
  void startGroupSavesAllParticipantsAndPublishesEvent() {
    List<UUID> participants = List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
    when(conversationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    Conversation result = service.startGroup(participants, "Coalition chat", null);

    assertThat(result.group()).isTrue();
    verify(conversationParticipantRepository, times(3)).save(any());
    ArgumentCaptor<ConversationCreated> captor = ArgumentCaptor.forClass(ConversationCreated.class);
    verify(eventPublisher).publish(captor.capture());
    assertThat(captor.getValue().group()).isTrue();
  }

  @Test
  void markReadUpdatesParticipant() {
    UUID conversationId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    ConversationParticipant participant = ConversationParticipant.join(conversationId, accountId, NOW);
    when(conversationParticipantRepository.findByConversationId(conversationId)).thenReturn(List.of(participant));

    service.markRead(conversationId, accountId);

    assertThat(participant.lastReadAt()).contains(NOW);
    verify(conversationParticipantRepository).save(participant);
  }
}
