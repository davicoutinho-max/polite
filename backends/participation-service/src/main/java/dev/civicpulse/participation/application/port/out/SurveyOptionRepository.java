package dev.civicpulse.participation.application.port.out;

import dev.civicpulse.participation.domain.model.SurveyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SurveyOptionRepository {

  SurveyOption save(SurveyOption option);

  Optional<SurveyOption> findById(UUID id);

  List<SurveyOption> findBySurveyId(UUID surveyId);
}
