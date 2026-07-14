package dev.civicpulse.messaging.application.port.in;

import dev.civicpulse.messaging.domain.model.Message;
import java.util.List;
import java.util.UUID;

public interface GetMessageUseCase {

  List<Message> listByConversation(UUID conversationId, int page, int pageSize);
}
