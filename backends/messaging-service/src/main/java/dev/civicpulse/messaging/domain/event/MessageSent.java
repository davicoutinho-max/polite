package dev.civicpulse.messaging.domain.event;

import java.time.Instant;
import java.util.UUID;

public record MessageSent(UUID conversationId, UUID messageId, UUID senderAccountId, Instant occurredAt) implements DomainEvent {

  @Override
  public String topic() {
    return "message-sent";
  }
}
