package dev.civicpulse.participation.adapter.out.persistence;

import dev.civicpulse.participation.domain.model.SurveyOption;
import org.springframework.stereotype.Component;

@Component
class SurveyOptionMapper {

  SurveyOption toDomain(SurveyOptionJpaEntity entity) {
    return SurveyOption.reconstitute(entity.getId(), entity.getSurveyId(), entity.getLabel(), entity.getVotesCount());
  }

  SurveyOptionJpaEntity toEntity(SurveyOption option) {
    return new SurveyOptionJpaEntity(option.id(), option.surveyId(), option.label(), option.votesCount());
  }
}
