package dev.civicpulse.participation.application.port.in;

import dev.civicpulse.participation.domain.model.Survey;
import java.util.List;
import java.util.UUID;

public interface ManageSurveyUseCase {

  Survey create(String question, String context, List<String> optionLabels);

  void vote(UUID surveyId, UUID citizenAccountId, UUID optionId);
}
