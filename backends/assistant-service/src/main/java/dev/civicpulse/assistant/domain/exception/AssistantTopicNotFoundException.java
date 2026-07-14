package dev.civicpulse.assistant.domain.exception;

import java.util.UUID;

public final class AssistantTopicNotFoundException extends RuntimeException {

  public AssistantTopicNotFoundException(UUID id) {
    super("No assistant topic found with id " + id);
  }
}
