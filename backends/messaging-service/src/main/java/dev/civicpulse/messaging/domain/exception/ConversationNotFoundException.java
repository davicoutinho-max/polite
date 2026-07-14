package dev.civicpulse.messaging.domain.exception;

import java.util.UUID;

public final class ConversationNotFoundException extends RuntimeException {

  public ConversationNotFoundException(UUID id) {
    super("No conversation found with id " + id);
  }
}
