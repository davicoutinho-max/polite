package dev.civicpulse.livestreaming.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import dev.civicpulse.livestreaming.application.port.out.LiveChatMessageRepository;
import dev.civicpulse.livestreaming.application.port.out.LiveSessionRepository;
import dev.civicpulse.livestreaming.application.port.out.LiveSessionStatsRepository;
import dev.civicpulse.livestreaming.domain.model.LiveChatMessage;
import dev.civicpulse.livestreaming.domain.model.LiveSession;
import dev.civicpulse.livestreaming.domain.model.LiveSessionStats;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** Exercises the real JPA/Postgres adapters against the shared local-dev database (see
 * identity-service's equivalent test for the rationale on why this isn't Testcontainers). */
@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:postgresql://localhost:5432/live_streaming_service",
      "spring.datasource.username=live_streaming_service_app",
      "spring.datasource.password=live_dev_pw"
    })
class LiveStreamingRepositoryIntegrationTest {

  @BeforeAll
  static void requireLocalPostgres() {
    boolean reachable;
    try (Socket socket = new Socket()) {
      socket.connect(new InetSocketAddress("localhost", 5432), 500);
      reachable = true;
    } catch (Exception e) {
      reachable = false;
    }
    assumeTrue(reachable, "Shared dev Postgres (localhost:5432) is not running — start it with "
        + "'docker compose up -d postgres' in backends/ to run this test");
  }

  @Autowired private LiveSessionRepository liveSessionRepository;
  @Autowired private LiveSessionStatsRepository liveSessionStatsRepository;
  @Autowired private LiveChatMessageRepository liveChatMessageRepository;

  @Test
  void savesAndRetrievesScheduledSession() {
    UUID id = UUID.randomUUID();
    LiveSession session = LiveSession.schedule(id, UUID.randomUUID(), "vid-1", "chan-1", Instant.now(), Instant.now());

    liveSessionRepository.save(session);

    assertThat(liveSessionRepository.findById(id)).isPresent().get().satisfies(found -> assertThat(found.videoId()).contains("vid-1"));
  }

  @Test
  void findLiveOnlyReturnsLiveSessions() {
    UUID scheduledId = UUID.randomUUID();
    UUID liveId = UUID.randomUUID();
    liveSessionRepository.save(LiveSession.schedule(scheduledId, UUID.randomUUID(), null, null, null, Instant.now()));
    LiveSession live = LiveSession.schedule(liveId, UUID.randomUUID(), null, null, null, Instant.now());
    live.start(Instant.now());
    liveSessionRepository.save(live);

    assertThat(liveSessionRepository.findLive()).extracting(LiveSession::id).contains(liveId).doesNotContain(scheduledId);
  }

  @Test
  void statsRoundTrip() {
    UUID sessionId = UUID.randomUUID();
    liveSessionRepository.save(LiveSession.schedule(sessionId, UUID.randomUUID(), null, null, null, Instant.now()));

    liveSessionStatsRepository.save(LiveSessionStats.compute(sessionId, 250, 40, 600, Instant.now()));

    assertThat(liveSessionStatsRepository.findBySessionId(sessionId))
        .isPresent()
        .get()
        .satisfies(found -> assertThat(found.totalUniqueViewers()).isEqualTo(250));
  }

  @Test
  void chatMessagePersistsAndCounts() {
    UUID sessionId = UUID.randomUUID();
    liveSessionRepository.save(LiveSession.schedule(sessionId, UUID.randomUUID(), null, null, null, Instant.now()));

    liveChatMessageRepository.save(LiveChatMessage.archive(sessionId, UUID.randomUUID(), "hi everyone", Instant.now()));

    assertThat(liveChatMessageRepository.findBySessionId(sessionId)).anySatisfy(m -> assertThat(m.body()).isEqualTo("hi everyone"));
    assertThat(liveChatMessageRepository.countBySessionId(sessionId)).isEqualTo(1);
  }
}
