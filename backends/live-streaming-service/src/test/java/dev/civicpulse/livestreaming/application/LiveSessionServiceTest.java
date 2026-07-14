package dev.civicpulse.livestreaming.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.civicpulse.livestreaming.application.port.out.EventPublisher;
import dev.civicpulse.livestreaming.application.port.out.LiveChatMessageRepository;
import dev.civicpulse.livestreaming.application.port.out.LiveSessionRepository;
import dev.civicpulse.livestreaming.application.port.out.LiveSessionStatsRepository;
import dev.civicpulse.livestreaming.domain.event.LiveSessionEnded;
import dev.civicpulse.livestreaming.domain.event.LiveSessionScheduled;
import dev.civicpulse.livestreaming.domain.event.LiveSessionStarted;
import dev.civicpulse.livestreaming.domain.model.LiveSession;
import dev.civicpulse.livestreaming.domain.model.LiveSessionStats;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LiveSessionServiceTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Mock private LiveSessionRepository liveSessionRepository;
  @Mock private LiveSessionStatsRepository liveSessionStatsRepository;
  @Mock private LiveChatMessageRepository liveChatMessageRepository;
  @Mock private EventPublisher eventPublisher;

  private LiveSessionService service;

  @BeforeEach
  void setUp() {
    service =
        new LiveSessionService(
            liveSessionRepository, liveSessionStatsRepository, liveChatMessageRepository, eventPublisher, Clock.fixed(NOW, ZoneOffset.UTC));
  }

  @Test
  void scheduleSavesSessionAndPublishesEvent() {
    when(liveSessionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    UUID hostId = UUID.randomUUID();

    LiveSession session = service.schedule(hostId, "vid-1", "chan-1", NOW);

    assertThat(session.hostAccountId()).isEqualTo(hostId);
    ArgumentCaptor<LiveSessionScheduled> captor = ArgumentCaptor.forClass(LiveSessionScheduled.class);
    verify(eventPublisher).publish(captor.capture());
    assertThat(captor.getValue().sessionId()).isEqualTo(session.id());
  }

  @Test
  void startTransitionsAndPublishesEvent() {
    UUID sessionId = UUID.randomUUID();
    LiveSession scheduled = LiveSession.schedule(sessionId, UUID.randomUUID(), null, null, null, NOW);
    when(liveSessionRepository.findById(sessionId)).thenReturn(Optional.of(scheduled));
    when(liveSessionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    LiveSession result = service.start(sessionId);

    assertThat(result.status().code()).isEqualTo("live");
    ArgumentCaptor<LiveSessionStarted> captor = ArgumentCaptor.forClass(LiveSessionStarted.class);
    verify(eventPublisher).publish(captor.capture());
    assertThat(captor.getValue().sessionId()).isEqualTo(sessionId);
  }

  @Test
  void endWritesStatsRollupAndPublishesEvent() {
    UUID sessionId = UUID.randomUUID();
    LiveSession live = LiveSession.schedule(sessionId, UUID.randomUUID(), null, null, null, NOW);
    live.start(NOW);
    when(liveSessionRepository.findById(sessionId)).thenReturn(Optional.of(live));
    when(liveSessionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(liveChatMessageRepository.countBySessionId(sessionId)).thenReturn(7L);

    LiveSession result = service.end(sessionId, 100, 300);

    assertThat(result.status().code()).isEqualTo("ended");
    ArgumentCaptor<LiveSessionStats> statsCaptor = ArgumentCaptor.forClass(LiveSessionStats.class);
    verify(liveSessionStatsRepository).save(statsCaptor.capture());
    assertThat(statsCaptor.getValue().totalUniqueViewers()).isEqualTo(100);
    assertThat(statsCaptor.getValue().totalChatMessages()).isEqualTo(7);
    verify(eventPublisher).publish(any(LiveSessionEnded.class));
  }

  @Test
  void endDefaultsUniqueViewersToZeroWhenNotProvided() {
    UUID sessionId = UUID.randomUUID();
    LiveSession live = LiveSession.schedule(sessionId, UUID.randomUUID(), null, null, null, NOW);
    live.start(NOW);
    when(liveSessionRepository.findById(sessionId)).thenReturn(Optional.of(live));
    when(liveSessionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(liveChatMessageRepository.countBySessionId(sessionId)).thenReturn(0L);

    service.end(sessionId, null, null);

    ArgumentCaptor<LiveSessionStats> statsCaptor = ArgumentCaptor.forClass(LiveSessionStats.class);
    verify(liveSessionStatsRepository).save(statsCaptor.capture());
    assertThat(statsCaptor.getValue().totalUniqueViewers()).isZero();
    assertThat(statsCaptor.getValue().avgWatchSeconds()).isEmpty();
  }

  @Test
  void attachPostSetsPostIdAndSaves() {
    UUID sessionId = UUID.randomUUID();
    UUID postId = UUID.randomUUID();
    LiveSession session = LiveSession.schedule(sessionId, UUID.randomUUID(), null, null, null, NOW);
    when(liveSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
    when(liveSessionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    LiveSession result = service.attachPost(sessionId, postId);

    assertThat(result.postId()).contains(postId);
  }

  @Test
  void recordViewerCountUpdatesPeak() {
    UUID sessionId = UUID.randomUUID();
    LiveSession session = LiveSession.schedule(sessionId, UUID.randomUUID(), null, null, null, NOW);
    session.start(NOW);
    when(liveSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
    when(liveSessionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    LiveSession result = service.recordViewerCount(sessionId, 42);

    assertThat(result.peakViewers()).isEqualTo(42);
  }
}
