package dev.civicpulse.participation.adapter.out.persistence;

import dev.civicpulse.participation.application.port.out.SurveyVoteRepository;
import dev.civicpulse.participation.domain.model.SurveyVote;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class SurveyVoteRepositoryAdapter implements SurveyVoteRepository {

  private final SurveyVoteJpaRepository jpaRepository;
  private final SurveyVoteMapper mapper;

  SurveyVoteRepositoryAdapter(SurveyVoteJpaRepository jpaRepository, SurveyVoteMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public SurveyVote save(SurveyVote vote) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(vote)));
  }

  @Override
  public boolean exists(UUID surveyId, UUID citizenAccountId) {
    return jpaRepository.existsBySurveyIdAndCitizenAccountId(surveyId, citizenAccountId);
  }
}
