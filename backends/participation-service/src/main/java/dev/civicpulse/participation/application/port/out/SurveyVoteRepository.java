package dev.civicpulse.participation.application.port.out;

import dev.civicpulse.participation.domain.model.SurveyVote;
import java.util.UUID;

public interface SurveyVoteRepository {

  SurveyVote save(SurveyVote vote);

  boolean exists(UUID surveyId, UUID citizenAccountId);
}
