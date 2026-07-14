package dev.civicpulse.notification.domain.exception;

import java.util.UUID;

public final class NotificationNotFoundException extends RuntimeException {

  public NotificationNotFoundException(UUID id) {
    super("No notification found with id " + id);
  }
}
