package dev.civicpulse.livestreaming.application.port.out;

import dev.civicpulse.livestreaming.domain.model.LiveSession;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LiveSessionRepository {

  LiveSession save(LiveSession session);

  Optional<LiveSession> findById(UUID id);

  /** Currently-live sessions only — backed by {@code idx_live_sessions_status}'s partial index. */
  List<LiveSession> findLive();
}
