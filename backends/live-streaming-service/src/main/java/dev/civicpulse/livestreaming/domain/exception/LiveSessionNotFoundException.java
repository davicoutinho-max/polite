package dev.civicpulse.livestreaming.domain.exception;

import java.util.UUID;

public final class LiveSessionNotFoundException extends RuntimeException {

  public LiveSessionNotFoundException(UUID id) {
    super("No live session found with id " + id);
  }
}
