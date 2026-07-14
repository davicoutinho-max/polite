package dev.civicpulse.participation.domain.exception;

import java.util.UUID;

public final class ConsultationNotFoundException extends RuntimeException {

  public ConsultationNotFoundException(UUID id) {
    super("No consultation found with id " + id);
  }
}
