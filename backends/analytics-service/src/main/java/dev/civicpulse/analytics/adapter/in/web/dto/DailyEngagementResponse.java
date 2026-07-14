package dev.civicpulse.analytics.adapter.in.web.dto;

import dev.civicpulse.analytics.application.port.out.EngagementEventRepository.DailyCount;
import java.time.LocalDate;

public record DailyEngagementResponse(LocalDate day, long likes, long comments) {

  public static DailyEngagementResponse from(DailyCount count) {
    return new DailyEngagementResponse(count.day(), count.likes(), count.comments());
  }
}
