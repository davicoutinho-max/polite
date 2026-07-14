package dev.civicpulse.participation.adapter.out.persistence;

import dev.civicpulse.participation.domain.model.SurveyVote;
import org.springframework.stereotype.Component;

@Component
class SurveyVoteMapper {

  SurveyVote toDomain(SurveyVoteJpaEntity entity) {
    return SurveyVote.reconstitute(entity.getSurveyId(), entity.getCitizenAccountId(), entity.getOptionId(), entity.getVotedAt());
  }

  SurveyVoteJpaEntity toEntity(SurveyVote vote) {
    return new SurveyVoteJpaEntity(vote.surveyId(), vote.citizenAccountId(), vote.optionId(), vote.votedAt());
  }
}
