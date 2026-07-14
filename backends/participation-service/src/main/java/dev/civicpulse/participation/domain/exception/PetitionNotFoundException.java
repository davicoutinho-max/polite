package dev.civicpulse.participation.domain.exception;

import java.util.UUID;

public final class PetitionNotFoundException extends RuntimeException {

  public PetitionNotFoundException(UUID id) {
    super("No petition found with id " + id);
  }
}
