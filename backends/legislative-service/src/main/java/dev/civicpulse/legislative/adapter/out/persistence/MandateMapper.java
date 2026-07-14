package dev.civicpulse.legislative.adapter.out.persistence;

import dev.civicpulse.legislative.domain.model.Mandate;
import org.springframework.stereotype.Component;

@Component
class MandateMapper {

  Mandate toDomain(MandateJpaEntity entity) {
    return Mandate.reconstitute(entity.getId(), entity.getPoliticianAccountId(), entity.getRole(), entity.getPeriod(), entity.isCurrent());
  }

  MandateJpaEntity toEntity(Mandate mandate) {
    return new MandateJpaEntity(
        mandate.id().orElse(null), mandate.politicianAccountId(), mandate.role(), mandate.period(), mandate.current());
  }
}
