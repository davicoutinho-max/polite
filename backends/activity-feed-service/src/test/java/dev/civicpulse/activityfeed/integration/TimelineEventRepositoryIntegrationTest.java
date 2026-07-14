package dev.civicpulse.activityfeed.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import dev.civicpulse.activityfeed.application.port.out.TimelineEventRepository;
import dev.civicpulse.activityfeed.domain.model.TimelineEvent;
import dev.civicpulse.activityfeed.domain.model.TimelineEventType;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** Exercises the real JPA/Postgres adapter against the shared local-dev database (see
 * identity-service's equivalent test for the rationale on why this isn't Testcontainers). */
@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:postgresql://localhost:5432/activity_feed_service",
      "spring.datasource.username=activity_feed_service_app",
      "spring.datasource.password=activity_feed_dev_pw"
    })
class TimelineEventRepositoryIntegrationTest {

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

  @Autowired private TimelineEventRepository timelineEventRepository;

  @Test
  void savesAndRetrievesEventsBySubjectMostRecentFirst() {
    UUID subject = UUID.randomUUID();
    Instant now = Instant.now();
    timelineEventRepository.save(
        TimelineEvent.record(subject, TimelineEventType.VOTE, "Voted YES", null, now.minusSeconds(60), "src:1:" + subject, subject, "Jane", now));
    timelineEventRepository.save(
        TimelineEvent.record(subject, TimelineEventType.PROJECT, "Filed PL 1", null, now, "src:2:" + subject, subject, "Jane", now));

    var results = timelineEventRepository.findBySubject(subject, 10);

    assertThat(results).hasSize(2);
    assertThat(results.get(0).title()).isEqualTo("Filed PL 1");
  }

  @Test
  void existsBySubjectAndSourceEventIdIsIdempotencyCheck() {
    UUID subject = UUID.randomUUID();
    Instant now = Instant.now();
    String sourceEventId = "src:idem:" + subject;
    assertThat(timelineEventRepository.existsBySubjectAndSourceEventId(subject, sourceEventId)).isFalse();

    timelineEventRepository.save(TimelineEvent.record(subject, TimelineEventType.VOTE, "Voted YES", null, now, sourceEventId, subject, null, now));

    assertThat(timelineEventRepository.existsBySubjectAndSourceEventId(subject, sourceEventId)).isTrue();
  }
}
