package dev.civicpulse.legislative.application.port.out;

import dev.civicpulse.legislative.domain.model.CareerMilestone;
import java.util.List;
import java.util.UUID;

public interface CareerMilestoneRepository {

  CareerMilestone save(CareerMilestone milestone);

  List<CareerMilestone> findByPolitician(UUID politicianAccountId);
}
