package dev.civicpulse.messaging.adapter.out.persistence;

import dev.civicpulse.messaging.application.port.out.MessageRepository;
import dev.civicpulse.messaging.domain.model.Message;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
class MessageRepositoryAdapter implements MessageRepository {

  private final MessageJpaRepository jpaRepository;
  private final MessageMapper mapper;

  MessageRepositoryAdapter(MessageJpaRepository jpaRepository, MessageMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public Message save(Message message) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(message)));
  }

  @Override
  public List<Message> findByConversationId(UUID conversationId, int page, int pageSize) {
    return jpaRepository.findByConversationId(conversationId, PageRequest.of(page, pageSize)).stream().map(mapper::toDomain).toList();
  }
}
