package dev.civicpulse.messaging.application;

import dev.civicpulse.messaging.application.port.in.EditMessageUseCase;
import dev.civicpulse.messaging.application.port.in.GetMessageUseCase;
import dev.civicpulse.messaging.application.port.in.SendMessageUseCase;
import dev.civicpulse.messaging.application.port.out.ConversationParticipantRepository;
import dev.civicpulse.messaging.application.port.out.ConversationRepository;
import dev.civicpulse.messaging.application.port.out.EventPublisher;
import dev.civicpulse.messaging.application.port.out.MessageRepository;
import dev.civicpulse.messaging.application.port.out.RealtimeMessagePublisher;
import dev.civicpulse.messaging.domain.event.MessageSent;
import dev.civicpulse.messaging.domain.exception.ConversationNotFoundException;
import dev.civicpulse.messaging.domain.exception.MessageNotFoundException;
import dev.civicpulse.messaging.domain.exception.NotAParticipantException;
import dev.civicpulse.messaging.domain.exception.NotMessageSenderException;
import dev.civicpulse.messaging.domain.model.AttachmentType;
import dev.civicpulse.messaging.domain.model.Conversation;
import dev.civicpulse.messaging.domain.model.Message;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MessageService implements SendMessageUseCase, GetMessageUseCase, EditMessageUseCase {

  private final ConversationRepository conversationRepository;
  private final ConversationParticipantRepository conversationParticipantRepository;
  private final MessageRepository messageRepository;
  private final EventPublisher eventPublisher;
  private final RealtimeMessagePublisher realtimePublisher;
  private final Clock clock;

  public MessageService(
      ConversationRepository conversationRepository,
      ConversationParticipantRepository conversationParticipantRepository,
      MessageRepository messageRepository,
      EventPublisher eventPublisher,
      RealtimeMessagePublisher realtimePublisher,
      Clock clock) {
    this.conversationRepository = conversationRepository;
    this.conversationParticipantRepository = conversationParticipantRepository;
    this.messageRepository = messageRepository;
    this.eventPublisher = eventPublisher;
    this.realtimePublisher = realtimePublisher;
    this.clock = clock;
  }

  @Override
  @Transactional
  public Message send(UUID conversationId, UUID senderAccountId, String body, UUID replyToMessageId) {
    Instant now = clock.instant();
    Message message =
        replyToMessageId != null
            ? Message.sendReply(UUID.randomUUID(), conversationId, senderAccountId, body, replyToMessageId, now)
            : Message.send(UUID.randomUUID(), conversationId, senderAccountId, body, now);
    return sendMessage(conversationId, senderAccountId, message, now);
  }

  @Override
  @Transactional
  public Message sendWithAttachment(
      UUID conversationId,
      UUID senderAccountId,
      String body,
      String attachmentUrl,
      AttachmentType attachmentType,
      String attachmentFileName,
      UUID replyToMessageId) {
    Instant now = clock.instant();
    Message message =
        Message.sendWithAttachment(
            UUID.randomUUID(), conversationId, senderAccountId, body, attachmentUrl, attachmentType, attachmentFileName, replyToMessageId, now);
    return sendMessage(conversationId, senderAccountId, message, now);
  }

  private Message sendMessage(UUID conversationId, UUID senderAccountId, Message toSend, Instant now) {
    Conversation conversation = conversationRepository.findById(conversationId).orElseThrow(() -> new ConversationNotFoundException(conversationId));
    if (!conversationParticipantRepository.exists(conversationId, senderAccountId)) {
      throw new NotAParticipantException();
    }

    Message message = messageRepository.save(toSend);
    conversation.recordMessageSent(now);
    conversationRepository.save(conversation);

    eventPublisher.publish(new MessageSent(conversationId, message.id(), senderAccountId, now));
    publishUpdate(message);
    return message;
  }

  @Override
  @Transactional(readOnly = true)
  public List<Message> listByConversation(UUID conversationId, int page, int pageSize) {
    return messageRepository.findByConversationId(conversationId, page, pageSize);
  }

  @Override
  @Transactional
  public Message edit(UUID messageId, UUID requesterAccountId, String newBody) {
    Message message = requireSender(messageId, requesterAccountId);
    message.edit(newBody, clock.instant());
    Message saved = messageRepository.save(message);
    publishUpdate(saved);
    return saved;
  }

  @Override
  @Transactional
  public Message delete(UUID messageId, UUID requesterAccountId) {
    Message message = requireSender(messageId, requesterAccountId);
    message.delete(clock.instant());
    Message saved = messageRepository.save(message);
    publishUpdate(saved);
    return saved;
  }

  private void publishUpdate(Message message) {
    realtimePublisher.messageUpdated(
        message.conversationId(),
        message.id(),
        message.senderAccountId(),
        message.body(),
        message.createdAt(),
        message.editedAt().orElse(null),
        message.isDeleted(),
        message.attachmentUrl().orElse(null),
        message.attachmentType().map(AttachmentType::code).orElse(null),
        message.attachmentFileName().orElse(null),
        message.replyToMessageId().orElse(null));
  }

  private Message requireSender(UUID messageId, UUID requesterAccountId) {
    Message message = messageRepository.findById(messageId).orElseThrow(() -> new MessageNotFoundException(messageId));
    if (!message.senderAccountId().equals(requesterAccountId)) {
      throw new NotMessageSenderException();
    }
    return message;
  }
}
