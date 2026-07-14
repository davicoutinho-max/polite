package dev.civicpulse.legislative.domain.exception;

import java.util.UUID;

public final class LegislativeItemNotFoundException extends RuntimeException {

  public LegislativeItemNotFoundException(UUID id) {
    super("No legislative item found with id " + id);
  }
}
