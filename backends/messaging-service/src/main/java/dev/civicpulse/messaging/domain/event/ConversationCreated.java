package dev.civicpulse.messaging.domain.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ConversationCreated(UUID conversationId, boolean group, List<UUID> participantAccountIds, Instant occurredAt)
    implements DomainEvent {

  @Override
  public String topic() {
    return "conversation-created";
  }
}
