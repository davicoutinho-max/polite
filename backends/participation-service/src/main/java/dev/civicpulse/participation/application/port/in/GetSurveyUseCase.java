package dev.civicpulse.participation.application.port.in;

import dev.civicpulse.participation.domain.model.Survey;
import dev.civicpulse.participation.domain.model.SurveyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GetSurveyUseCase {

  Survey getById(UUID id);

  List<Survey> list(int page, int pageSize);

  List<SurveyOption> listOptions(UUID surveyId);

  Optional<UUID> getMyVote(UUID surveyId, UUID citizenAccountId);
}
