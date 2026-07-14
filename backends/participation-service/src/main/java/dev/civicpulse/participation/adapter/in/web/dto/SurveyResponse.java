package dev.civicpulse.participation.adapter.in.web.dto;

import dev.civicpulse.participation.domain.model.Survey;
import java.util.UUID;

public record SurveyResponse(UUID id, String question, String context) {

  public static SurveyResponse from(Survey survey) {
    return new SurveyResponse(survey.id(), survey.question(), survey.context().orElse(null));
  }
}
