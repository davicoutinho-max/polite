package dev.civicpulse.legislative.application;

import dev.civicpulse.legislative.application.port.in.CareerUseCase;
import dev.civicpulse.legislative.application.port.out.CareerMilestoneRepository;
import dev.civicpulse.legislative.domain.model.CareerMilestone;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CareerService implements CareerUseCase {

  private final CareerMilestoneRepository careerMilestoneRepository;

  public CareerService(CareerMilestoneRepository careerMilestoneRepository) {
    this.careerMilestoneRepository = careerMilestoneRepository;
  }

  @Override
  @Transactional
  public CareerMilestone addMilestone(UUID politicianAccountId, short year, String title, String detail) {
    return careerMilestoneRepository.save(CareerMilestone.add(politicianAccountId, year, title, detail));
  }

  @Override
  public List<CareerMilestone> getMilestones(UUID politicianAccountId) {
    return careerMilestoneRepository.findByPolitician(politicianAccountId);
  }
}
