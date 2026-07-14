package dev.civicpulse.participation.adapter.out.persistence;

import dev.civicpulse.participation.domain.model.ConsultationResponse;
import org.springframework.stereotype.Component;

@Component
class ConsultationResponseMapper {

  ConsultationResponse toDomain(ConsultationResponseJpaEntity entity) {
    return ConsultationResponse.reconstitute(entity.getConsultationId(), entity.getCitizenAccountId(), entity.getStance(), entity.getUpdatedAt());
  }

  ConsultationResponseJpaEntity toEntity(ConsultationResponse response) {
    return new ConsultationResponseJpaEntity(response.consultationId(), response.citizenAccountId(), response.stance(), response.updatedAt());
  }
}
