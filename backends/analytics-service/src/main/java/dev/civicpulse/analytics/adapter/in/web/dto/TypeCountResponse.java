package dev.civicpulse.analytics.adapter.in.web.dto;

import dev.civicpulse.analytics.application.port.out.EngagementEventRepository.TypeCount;

public record TypeCountResponse(String key, long count) {

  public static TypeCountResponse from(TypeCount typeCount) {
    return new TypeCountResponse(typeCount.key(), typeCount.count());
  }
}
