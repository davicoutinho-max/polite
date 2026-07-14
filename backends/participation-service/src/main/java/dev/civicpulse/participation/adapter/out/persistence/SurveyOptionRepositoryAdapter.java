package dev.civicpulse.participation.adapter.out.persistence;

import dev.civicpulse.participation.application.port.out.SurveyOptionRepository;
import dev.civicpulse.participation.domain.model.SurveyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class SurveyOptionRepositoryAdapter implements SurveyOptionRepository {

  private final SurveyOptionJpaRepository jpaRepository;
  private final SurveyOptionMapper mapper;

  SurveyOptionRepositoryAdapter(SurveyOptionJpaRepository jpaRepository, SurveyOptionMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public SurveyOption save(SurveyOption option) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(option)));
  }

  @Override
  public Optional<SurveyOption> findById(UUID id) {
    return jpaRepository.findById(id).map(mapper::toDomain);
  }

  @Override
  public List<SurveyOption> findBySurveyId(UUID surveyId) {
    return jpaRepository.findBySurveyId(surveyId).stream().map(mapper::toDomain).toList();
  }
}
