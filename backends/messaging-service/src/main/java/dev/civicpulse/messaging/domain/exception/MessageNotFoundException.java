package dev.civicpulse.messaging.domain.exception;

import java.util.UUID;

public final class MessageNotFoundException extends RuntimeException {

  public MessageNotFoundException(UUID id) {
    super("No message found with id " + id);
  }
}
