package dev.civicpulse.elections.domain.exception;

import java.util.UUID;

public final class ElectionNotFoundException extends RuntimeException {

  public ElectionNotFoundException(UUID id) {
    super("No election found with id " + id);
  }
}
