package dev.civicpulse.messaging.application.port.out;

import dev.civicpulse.messaging.domain.model.Message;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageRepository {

  Message save(Message message);

  Optional<Message> findById(UUID id);

  List<Message> findByConversationId(UUID conversationId, int page, int pageSize);

  void deleteByConversationId(UUID conversationId);
}
