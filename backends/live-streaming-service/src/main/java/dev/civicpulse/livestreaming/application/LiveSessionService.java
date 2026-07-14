package dev.civicpulse.livestreaming.application;

import dev.civicpulse.livestreaming.application.port.in.ManageLiveSessionUseCase;
import dev.civicpulse.livestreaming.application.port.out.EventPublisher;
import dev.civicpulse.livestreaming.application.port.out.LiveChatMessageRepository;
import dev.civicpulse.livestreaming.application.port.out.LiveSessionRepository;
import dev.civicpulse.livestreaming.application.port.out.LiveSessionStatsRepository;
import dev.civicpulse.livestreaming.domain.event.LiveSessionEnded;
import dev.civicpulse.livestreaming.domain.event.LiveSessionScheduled;
import dev.civicpulse.livestreaming.domain.event.LiveSessionStarted;
import dev.civicpulse.livestreaming.domain.exception.LiveSessionNotFoundException;
import dev.civicpulse.livestreaming.domain.model.LiveSession;
import dev.civicpulse.livestreaming.domain.model.LiveSessionStats;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LiveSessionService implements ManageLiveSessionUseCase {

  private final LiveSessionRepository liveSessionRepository;
  private final LiveSessionStatsRepository liveSessionStatsRepository;
  private final LiveChatMessageRepository liveChatMessageRepository;
  private final EventPublisher eventPublisher;
  private final Clock clock;

  public LiveSessionService(
      LiveSessionRepository liveSessionRepository,
      LiveSessionStatsRepository liveSessionStatsRepository,
      LiveChatMessageRepository liveChatMessageRepository,
      EventPublisher eventPublisher,
      Clock clock) {
    this.liveSessionRepository = liveSessionRepository;
    this.liveSessionStatsRepository = liveSessionStatsRepository;
    this.liveChatMessageRepository = liveChatMessageRepository;
    this.eventPublisher = eventPublisher;
    this.clock = clock;
  }

  @Override
  @Transactional
  public LiveSession schedule(UUID hostAccountId, String videoId, String channelId, Instant scheduledFor) {
    Instant now = clock.instant();
    LiveSession session = LiveSession.schedule(UUID.randomUUID(), hostAccountId, videoId, channelId, scheduledFor, now);
    LiveSession saved = liveSessionRepository.save(session);
    eventPublisher.publish(new LiveSessionScheduled(saved.id(), hostAccountId, scheduledFor, now));
    return saved;
  }

  @Override
  @Transactional
  public LiveSession start(UUID sessionId) {
    LiveSession session = findOrThrow(sessionId);
    Instant now = clock.instant();
    session.start(now);
    LiveSession saved = liveSessionRepository.save(session);
    eventPublisher.publish(new LiveSessionStarted(saved.id(), saved.hostAccountId(), now));
    return saved;
  }

  @Override
  @Transactional
  public LiveSession end(UUID sessionId, Integer totalUniqueViewers, Integer avgWatchSeconds) {
    LiveSession session = findOrThrow(sessionId);
    Instant now = clock.instant();
    session.end(now);
    LiveSession saved = liveSessionRepository.save(session);

    long chatCount = liveChatMessageRepository.countBySessionId(sessionId);
    liveSessionStatsRepository.save(
        LiveSessionStats.compute(
            sessionId, totalUniqueViewers == null ? 0 : totalUniqueViewers, (int) chatCount, avgWatchSeconds, now));

    eventPublisher.publish(new LiveSessionEnded(saved.id(), saved.hostAccountId(), saved.peakViewers(), now));
    return saved;
  }

  @Override
  @Transactional
  public LiveSession attachPost(UUID sessionId, UUID postId) {
    LiveSession session = findOrThrow(sessionId);
    session.attachPost(postId);
    return liveSessionRepository.save(session);
  }

  @Override
  @Transactional
  public LiveSession recordViewerCount(UUID sessionId, int currentViewers) {
    LiveSession session = findOrThrow(sessionId);
    session.recordViewerCount(currentViewers);
    return liveSessionRepository.save(session);
  }

  private LiveSession findOrThrow(UUID sessionId) {
    return liveSessionRepository.findById(sessionId).orElseThrow(() -> new LiveSessionNotFoundException(sessionId));
  }
}
