package dev.civicpulse.livestreaming.application.port.out;

import dev.civicpulse.livestreaming.domain.model.LiveSessionStats;
import java.util.Optional;
import java.util.UUID;

public interface LiveSessionStatsRepository {

  LiveSessionStats save(LiveSessionStats stats);

  Optional<LiveSessionStats> findBySessionId(UUID liveSessionId);
}
