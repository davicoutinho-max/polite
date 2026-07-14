package dev.civicpulse.participation.adapter.out.persistence;

import dev.civicpulse.participation.application.port.out.SurveyRepository;
import dev.civicpulse.participation.domain.model.Survey;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
class SurveyRepositoryAdapter implements SurveyRepository {

  private final SurveyJpaRepository jpaRepository;
  private final SurveyMapper mapper;

  SurveyRepositoryAdapter(SurveyJpaRepository jpaRepository, SurveyMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public Survey save(Survey survey) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(survey)));
  }

  @Override
  public Optional<Survey> findById(UUID id) {
    return jpaRepository.findById(id).map(mapper::toDomain);
  }

  @Override
  public List<Survey> findAll(int page, int pageSize) {
    return jpaRepository.findAllOrdered(PageRequest.of(page, pageSize)).stream().map(mapper::toDomain).toList();
  }
}
