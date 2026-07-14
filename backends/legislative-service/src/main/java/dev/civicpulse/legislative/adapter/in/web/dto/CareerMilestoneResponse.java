package dev.civicpulse.legislative.adapter.in.web.dto;

import dev.civicpulse.legislative.domain.model.CareerMilestone;
import java.util.UUID;

public record CareerMilestoneResponse(UUID id, short year, String title, String detail) {

  public static CareerMilestoneResponse from(CareerMilestone milestone) {
    return new CareerMilestoneResponse(milestone.id().orElse(null), milestone.year(), milestone.title(), milestone.detail().orElse(null));
  }
}
