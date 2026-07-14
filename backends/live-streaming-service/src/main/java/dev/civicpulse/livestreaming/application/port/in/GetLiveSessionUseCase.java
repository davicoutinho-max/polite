package dev.civicpulse.livestreaming.application.port.in;

import dev.civicpulse.livestreaming.domain.model.LiveSession;
import dev.civicpulse.livestreaming.domain.model.LiveSessionStats;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GetLiveSessionUseCase {

  LiveSession getById(UUID id);

  List<LiveSession> listLive();

  Optional<LiveSessionStats> getStats(UUID sessionId);
}
