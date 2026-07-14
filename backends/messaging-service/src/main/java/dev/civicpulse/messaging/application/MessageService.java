package dev.civicpulse.messaging.application;

import dev.civicpulse.messaging.application.port.in.GetMessageUseCase;
import dev.civicpulse.messaging.application.port.in.SendMessageUseCase;
import dev.civicpulse.messaging.application.port.out.ConversationParticipantRepository;
import dev.civicpulse.messaging.application.port.out.ConversationRepository;
import dev.civicpulse.messaging.application.port.out.EventPublisher;
import dev.civicpulse.messaging.application.port.out.MessageRepository;
import dev.civicpulse.messaging.domain.event.MessageSent;
import dev.civicpulse.messaging.domain.exception.ConversationNotFoundException;
import dev.civicpulse.messaging.domain.exception.NotAParticipantException;
import dev.civicpulse.messaging.domain.model.Conversation;
import dev.civicpulse.messaging.domain.model.Message;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MessageService implements SendMessageUseCase, GetMessageUseCase {

  private final ConversationRepository conversationRepository;
  private final ConversationParticipantRepository conversationParticipantRepository;
  private final MessageRepository messageRepository;
  private final EventPublisher eventPublisher;
  private final Clock clock;

  public MessageService(
      ConversationRepository conversationRepository,
      ConversationParticipantRepository conversationParticipantRepository,
      MessageRepository messageRepository,
      EventPublisher eventPublisher,
      Clock clock) {
    this.conversationRepository = conversationRepository;
    this.conversationParticipantRepository = conversationParticipantRepository;
    this.messageRepository = messageRepository;
    this.eventPublisher = eventPublisher;
    this.clock = clock;
  }

  @Override
  @Transactional
  public Message send(UUID conversationId, UUID senderAccountId, String body) {
    Conversation conversation = conversationRepository.findById(conversationId).orElseThrow(() -> new ConversationNotFoundException(conversationId));
    if (!conversationParticipantRepository.exists(conversationId, senderAccountId)) {
      throw new NotAParticipantException();
    }

    Instant now = clock.instant();
    Message message = messageRepository.save(Message.send(UUID.randomUUID(), conversationId, senderAccountId, body, now));
    conversation.recordMessageSent(now);
    conversationRepository.save(conversation);

    eventPublisher.publish(new MessageSent(conversationId, message.id(), senderAccountId, now));
    return message;
  }

  @Override
  @Transactional(readOnly = true)
  public List<Message> listByConversation(UUID conversationId, int page, int pageSize) {
    return messageRepository.findByConversationId(conversationId, page, pageSize);
  }
}
