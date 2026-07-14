package dev.civicpulse.messaging.application;

import dev.civicpulse.messaging.application.port.in.GetConversationUseCase;
import dev.civicpulse.messaging.application.port.in.ManageConversationUseCase;
import dev.civicpulse.messaging.application.port.out.ConversationParticipantRepository;
import dev.civicpulse.messaging.application.port.out.ConversationRepository;
import dev.civicpulse.messaging.application.port.out.EventPublisher;
import dev.civicpulse.messaging.domain.event.ConversationCreated;
import dev.civicpulse.messaging.domain.exception.ConversationNotFoundException;
import dev.civicpulse.messaging.domain.model.Conversation;
import dev.civicpulse.messaging.domain.model.ConversationParticipant;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConversationService implements ManageConversationUseCase, GetConversationUseCase {

  private final ConversationRepository conversationRepository;
  private final ConversationParticipantRepository conversationParticipantRepository;
  private final EventPublisher eventPublisher;
  private final Clock clock;

  public ConversationService(
      ConversationRepository conversationRepository,
      ConversationParticipantRepository conversationParticipantRepository,
      EventPublisher eventPublisher,
      Clock clock) {
    this.conversationRepository = conversationRepository;
    this.conversationParticipantRepository = conversationParticipantRepository;
    this.eventPublisher = eventPublisher;
    this.clock = clock;
  }

  @Override
  @Transactional
  public Conversation startDirect(UUID accountA, UUID accountB) {
    var existing = conversationRepository.findDirectBetween(accountA, accountB);
    if (existing.isPresent()) {
      return existing.get();
    }

    Instant now = clock.instant();
    Conversation conversation = conversationRepository.save(Conversation.createDirect(UUID.randomUUID(), now));
    conversationParticipantRepository.save(ConversationParticipant.join(conversation.id(), accountA, now));
    conversationParticipantRepository.save(ConversationParticipant.join(conversation.id(), accountB, now));

    eventPublisher.publish(new ConversationCreated(conversation.id(), false, List.of(accountA, accountB), now));
    return conversation;
  }

  @Override
  @Transactional
  public Conversation startGroup(List<UUID> participantAccountIds, String groupName, String groupAvatarUrl) {
    Instant now = clock.instant();
    Conversation conversation = conversationRepository.save(Conversation.createGroup(UUID.randomUUID(), groupName, groupAvatarUrl, now));
    for (UUID accountId : participantAccountIds) {
      conversationParticipantRepository.save(ConversationParticipant.join(conversation.id(), accountId, now));
    }

    eventPublisher.publish(new ConversationCreated(conversation.id(), true, List.copyOf(participantAccountIds), now));
    return conversation;
  }

  @Override
  @Transactional
  public void markRead(UUID conversationId, UUID accountId) {
    ConversationParticipant participant =
        conversationParticipantRepository.findByConversationId(conversationId).stream()
            .filter(p -> p.accountId().equals(accountId))
            .findFirst()
            .orElseThrow(() -> new ConversationNotFoundException(conversationId));
    participant.markRead(clock.instant());
    conversationParticipantRepository.save(participant);
  }

  @Override
  @Transactional(readOnly = true)
  public Conversation getById(UUID id) {
    return conversationRepository.findById(id).orElseThrow(() -> new ConversationNotFoundException(id));
  }

  @Override
  @Transactional(readOnly = true)
  public List<Conversation> listByParticipant(UUID accountId) {
    return conversationRepository.findByParticipant(accountId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ConversationParticipant> listParticipants(UUID conversationId) {
    return conversationParticipantRepository.findByConversationId(conversationId);
  }
}
