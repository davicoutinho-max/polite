package dev.civicpulse.livestreaming.adapter.in.web.dto;

import dev.civicpulse.livestreaming.domain.model.LiveSessionStats;
import java.time.Instant;
import java.util.UUID;

public record LiveSessionStatsResponse(
    UUID liveSessionId, int totalUniqueViewers, int totalChatMessages, Integer avgWatchSeconds, Instant computedAt) {

  public static LiveSessionStatsResponse from(LiveSessionStats stats) {
    return new LiveSessionStatsResponse(
        stats.liveSessionId(), stats.totalUniqueViewers(), stats.totalChatMessages(), stats.avgWatchSeconds().orElse(null), stats.computedAt());
  }
}
