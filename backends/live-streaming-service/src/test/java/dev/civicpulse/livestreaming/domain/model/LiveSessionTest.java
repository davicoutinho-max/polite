package dev.civicpulse.livestreaming.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.civicpulse.livestreaming.domain.exception.InvalidLiveSessionTransitionException;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class LiveSessionTest {

  private static final Instant T0 = Instant.parse("2026-01-01T00:00:00Z");
  private static final Instant T1 = Instant.parse("2026-01-01T01:00:00Z");
  private static final Instant T2 = Instant.parse("2026-01-01T02:00:00Z");

  @Test
  void scheduleStartsInScheduledStatus() {
    LiveSession session = LiveSession.schedule(UUID.randomUUID(), UUID.randomUUID(), "vid-1", "chan-1", T1, T0);

    assertThat(session.status()).isEqualTo(LiveSessionStatus.SCHEDULED);
    assertThat(session.scheduledFor()).contains(T1);
    assertThat(session.startedAt()).isEmpty();
    assertThat(session.endedAt()).isEmpty();
    assertThat(session.peakViewers()).isZero();
  }

  @Test
  void startTransitionsFromScheduledToLive() {
    LiveSession session = LiveSession.schedule(UUID.randomUUID(), UUID.randomUUID(), null, null, null, T0);

    session.start(T1);

    assertThat(session.status()).isEqualTo(LiveSessionStatus.LIVE);
    assertThat(session.startedAt()).contains(T1);
  }

  @Test
  void startTwiceThrows() {
    LiveSession session = LiveSession.schedule(UUID.randomUUID(), UUID.randomUUID(), null, null, null, T0);
    session.start(T1);

    assertThatThrownBy(() -> session.start(T2)).isInstanceOf(InvalidLiveSessionTransitionException.class);
  }

  @Test
  void endRequiresLiveStatus() {
    LiveSession session = LiveSession.schedule(UUID.randomUUID(), UUID.randomUUID(), null, null, null, T0);

    assertThatThrownBy(() -> session.end(T1)).isInstanceOf(InvalidLiveSessionTransitionException.class);
  }

  @Test
  void endTransitionsFromLiveToEnded() {
    LiveSession session = LiveSession.schedule(UUID.randomUUID(), UUID.randomUUID(), null, null, null, T0);
    session.start(T1);

    session.end(T2);

    assertThat(session.status()).isEqualTo(LiveSessionStatus.ENDED);
    assertThat(session.endedAt()).contains(T2);
  }

  @Test
  void recordViewerCountRequiresLiveStatus() {
    LiveSession session = LiveSession.schedule(UUID.randomUUID(), UUID.randomUUID(), null, null, null, T0);

    assertThatThrownBy(() -> session.recordViewerCount(10)).isInstanceOf(InvalidLiveSessionTransitionException.class);
  }

  @Test
  void recordViewerCountTracksPeak() {
    LiveSession session = LiveSession.schedule(UUID.randomUUID(), UUID.randomUUID(), null, null, null, T0);
    session.start(T1);

    session.recordViewerCount(50);
    session.recordViewerCount(30);
    session.recordViewerCount(80);

    assertThat(session.peakViewers()).isEqualTo(80);
  }

  @Test
  void attachPostSetsPostId() {
    LiveSession session = LiveSession.schedule(UUID.randomUUID(), UUID.randomUUID(), null, null, null, T0);
    UUID postId = UUID.randomUUID();

    session.attachPost(postId);

    assertThat(session.postId()).contains(postId);
  }

  @Test
  void equalityIsBasedOnId() {
    UUID id = UUID.randomUUID();
    LiveSession a = LiveSession.schedule(id, UUID.randomUUID(), null, null, null, T0);
    LiveSession b = LiveSession.reconstitute(id, UUID.randomUUID(), null, null, null, LiveSessionStatus.LIVE, null, T0, null, 5, T0);

    assertThat(a).isEqualTo(b);
    assertThat(a.hashCode()).isEqualTo(b.hashCode());
  }
}
