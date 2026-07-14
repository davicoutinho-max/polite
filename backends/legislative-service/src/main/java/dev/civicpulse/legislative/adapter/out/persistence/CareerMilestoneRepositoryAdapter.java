package dev.civicpulse.legislative.adapter.out.persistence;

import dev.civicpulse.legislative.application.port.out.CareerMilestoneRepository;
import dev.civicpulse.legislative.domain.model.CareerMilestone;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class CareerMilestoneRepositoryAdapter implements CareerMilestoneRepository {

  private final CareerMilestoneJpaRepository jpaRepository;
  private final CareerMilestoneMapper mapper;

  CareerMilestoneRepositoryAdapter(CareerMilestoneJpaRepository jpaRepository, CareerMilestoneMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public CareerMilestone save(CareerMilestone milestone) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(milestone)));
  }

  @Override
  public List<CareerMilestone> findByPolitician(UUID politicianAccountId) {
    return jpaRepository.findByPoliticianAccountIdOrderByYear(politicianAccountId).stream().map(mapper::toDomain).toList();
  }
}
