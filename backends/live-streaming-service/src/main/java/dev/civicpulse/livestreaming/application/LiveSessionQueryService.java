package dev.civicpulse.livestreaming.application;

import dev.civicpulse.livestreaming.application.port.in.GetLiveSessionUseCase;
import dev.civicpulse.livestreaming.application.port.out.LiveSessionRepository;
import dev.civicpulse.livestreaming.application.port.out.LiveSessionStatsRepository;
import dev.civicpulse.livestreaming.domain.exception.LiveSessionNotFoundException;
import dev.civicpulse.livestreaming.domain.model.LiveSession;
import dev.civicpulse.livestreaming.domain.model.LiveSessionStats;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LiveSessionQueryService implements GetLiveSessionUseCase {

  private final LiveSessionRepository liveSessionRepository;
  private final LiveSessionStatsRepository liveSessionStatsRepository;

  public LiveSessionQueryService(LiveSessionRepository liveSessionRepository, LiveSessionStatsRepository liveSessionStatsRepository) {
    this.liveSessionRepository = liveSessionRepository;
    this.liveSessionStatsRepository = liveSessionStatsRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public LiveSession getById(UUID id) {
    return liveSessionRepository.findById(id).orElseThrow(() -> new LiveSessionNotFoundException(id));
  }

  @Override
  @Transactional(readOnly = true)
  public List<LiveSession> listLive() {
    return liveSessionRepository.findLive();
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<LiveSessionStats> getStats(UUID sessionId) {
    return liveSessionStatsRepository.findBySessionId(sessionId);
  }
}
