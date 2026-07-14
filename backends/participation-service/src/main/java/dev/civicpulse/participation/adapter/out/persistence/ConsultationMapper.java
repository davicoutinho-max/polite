package dev.civicpulse.participation.adapter.out.persistence;

import dev.civicpulse.participation.domain.model.Consultation;
import org.springframework.stereotype.Component;

@Component
class ConsultationMapper {

  Consultation toDomain(ConsultationJpaEntity entity) {
    return Consultation.reconstitute(entity.getId(), entity.getTitle(), entity.getDescription(), entity.getDeadline(), entity.getResponsesCount());
  }

  ConsultationJpaEntity toEntity(Consultation consultation) {
    return new ConsultationJpaEntity(
        consultation.id(), consultation.title(), consultation.description().orElse(null), consultation.deadline().orElse(null),
        consultation.responsesCount());
  }
}
