package dev.civicpulse.participation.domain.exception;

import java.util.UUID;

public final class SurveyOptionNotFoundException extends RuntimeException {

  public SurveyOptionNotFoundException(UUID id) {
    super("No survey option found with id " + id);
  }
}
