package dev.civicpulse.participation.domain.exception;

import java.util.UUID;

public final class SurveyNotFoundException extends RuntimeException {

  public SurveyNotFoundException(UUID id) {
    super("No survey found with id " + id);
  }
}
