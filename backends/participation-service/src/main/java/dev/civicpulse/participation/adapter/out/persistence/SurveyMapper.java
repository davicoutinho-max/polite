package dev.civicpulse.participation.adapter.out.persistence;

import dev.civicpulse.participation.domain.model.Survey;
import org.springframework.stereotype.Component;

@Component
class SurveyMapper {

  Survey toDomain(SurveyJpaEntity entity) {
    return Survey.reconstitute(entity.getId(), entity.getQuestion(), entity.getContext());
  }

  SurveyJpaEntity toEntity(Survey survey) {
    return new SurveyJpaEntity(survey.id(), survey.question(), survey.context().orElse(null));
  }
}
