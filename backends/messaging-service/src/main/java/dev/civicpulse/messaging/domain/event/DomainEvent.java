package dev.civicpulse.messaging.domain.event;

import java.time.Instant;

public sealed interface DomainEvent permits ConversationCreated, MessageSent {

  String topic();

  Instant occurredAt();
}
