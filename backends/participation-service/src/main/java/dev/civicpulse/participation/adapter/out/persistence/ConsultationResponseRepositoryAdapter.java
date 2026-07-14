package dev.civicpulse.participation.adapter.out.persistence;

import dev.civicpulse.participation.application.port.out.ConsultationResponseRepository;
import dev.civicpulse.participation.domain.model.ConsultationResponse;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class ConsultationResponseRepositoryAdapter implements ConsultationResponseRepository {

  private final ConsultationResponseJpaRepository jpaRepository;
  private final ConsultationResponseMapper mapper;

  ConsultationResponseRepositoryAdapter(ConsultationResponseJpaRepository jpaRepository, ConsultationResponseMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public ConsultationResponse save(ConsultationResponse response) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(response)));
  }

  @Override
  public Optional<ConsultationResponse> findByConsultationAndCitizen(UUID consultationId, UUID citizenAccountId) {
    return jpaRepository.findByConsultationIdAndCitizenAccountId(consultationId, citizenAccountId).map(mapper::toDomain);
  }
}
