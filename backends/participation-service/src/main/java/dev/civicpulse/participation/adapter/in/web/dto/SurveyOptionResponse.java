package dev.civicpulse.participation.adapter.in.web.dto;

import dev.civicpulse.participation.domain.model.SurveyOption;
import java.util.UUID;

public record SurveyOptionResponse(UUID id, UUID surveyId, String label, int votesCount) {

  public static SurveyOptionResponse from(SurveyOption option) {
    return new SurveyOptionResponse(option.id(), option.surveyId(), option.label(), option.votesCount());
  }
}
