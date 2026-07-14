package dev.civicpulse.messaging.application.port.in;

import dev.civicpulse.messaging.domain.model.Message;
import java.util.UUID;

public interface SendMessageUseCase {

  Message send(UUID conversationId, UUID senderAccountId, String body);
}
