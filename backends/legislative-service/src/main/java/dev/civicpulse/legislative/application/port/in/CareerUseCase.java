package dev.civicpulse.legislative.application.port.in;

import dev.civicpulse.legislative.domain.model.CareerMilestone;
import java.util.List;
import java.util.UUID;

public interface CareerUseCase {

  CareerMilestone addMilestone(UUID politicianAccountId, short year, String title, String detail);

  List<CareerMilestone> getMilestones(UUID politicianAccountId);
}
