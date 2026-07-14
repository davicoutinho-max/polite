package dev.civicpulse.livestreaming.adapter.out.persistence;

import dev.civicpulse.livestreaming.domain.model.LiveSessionStats;
import org.springframework.stereotype.Component;

@Component
class LiveSessionStatsMapper {

  LiveSessionStats toDomain(LiveSessionStatsJpaEntity entity) {
    return LiveSessionStats.reconstitute(
        entity.getLiveSessionId(), entity.getTotalUniqueViewers(), entity.getTotalChatMessages(), entity.getAvgWatchSeconds(), entity.getComputedAt());
  }

  LiveSessionStatsJpaEntity toEntity(LiveSessionStats stats) {
    return new LiveSessionStatsJpaEntity(
        stats.liveSessionId(), stats.totalUniqueViewers(), stats.totalChatMessages(), stats.avgWatchSeconds().orElse(null), stats.computedAt());
  }
}
