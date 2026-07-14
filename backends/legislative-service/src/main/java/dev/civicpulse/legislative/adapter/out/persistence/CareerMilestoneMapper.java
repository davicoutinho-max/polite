package dev.civicpulse.legislative.adapter.out.persistence;

import dev.civicpulse.legislative.domain.model.CareerMilestone;
import org.springframework.stereotype.Component;

@Component
class CareerMilestoneMapper {

  CareerMilestone toDomain(CareerMilestoneJpaEntity entity) {
    return CareerMilestone.reconstitute(
        entity.getId(), entity.getPoliticianAccountId(), entity.getYear(), entity.getTitle(), entity.getDetail());
  }

  CareerMilestoneJpaEntity toEntity(CareerMilestone milestone) {
    return new CareerMilestoneJpaEntity(
        milestone.id().orElse(null), milestone.politicianAccountId(), milestone.year(), milestone.title(), milestone.detail().orElse(null));
  }
}
