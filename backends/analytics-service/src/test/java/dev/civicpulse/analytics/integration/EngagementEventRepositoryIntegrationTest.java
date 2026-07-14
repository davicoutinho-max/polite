package dev.civicpulse.analytics.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import dev.civicpulse.analytics.application.port.out.EngagementEventRepository;
import dev.civicpulse.analytics.domain.model.EngagementEvent;
import dev.civicpulse.analytics.domain.model.EngagementEventType;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** Exercises the real JPA/Postgres adapter against the shared local-dev database (see
 * identity-service's equivalent test for the rationale on why this isn't Testcontainers). */
@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:postgresql://localhost:5432/analytics_service",
      "spring.datasource.username=analytics_service_app",
      "spring.datasource.password=analytics_dev_pw"
    })
class EngagementEventRepositoryIntegrationTest {

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

  @Autowired private EngagementEventRepository engagementEventRepository;

  @Test
  void savesAndComputesDistinctActorReach() {
    UUID author = UUID.randomUUID();
    UUID actor1 = UUID.randomUUID();
    UUID actor2 = UUID.randomUUID();
    Instant now = Instant.now();

    engagementEventRepository.save(
        EngagementEvent.record(author, actor1, "citizen", EngagementEventType.LIKE, "text", now, "src:1:" + author, now));
    engagementEventRepository.save(
        EngagementEvent.record(author, actor1, "citizen", EngagementEventType.COMMENT, "text", now, "src:2:" + author, now));
    engagementEventRepository.save(
        EngagementEvent.record(author, actor2, "politician", EngagementEventType.LIKE, "text", now, "src:3:" + author, now));

    long reach = engagementEventRepository.countDistinctActors(author, List.of("like", "comment"));
    assertThat(reach).isEqualTo(2L);

    long likeCount = engagementEventRepository.countByAuthorAndType(author, "like");
    assertThat(likeCount).isEqualTo(2L);
  }

  @Test
  void byContentTypeAndByAccountTypeBreakdowns() {
    UUID author = UUID.randomUUID();
    Instant now = Instant.now();
    engagementEventRepository.save(
        EngagementEvent.record(author, UUID.randomUUID(), "citizen", EngagementEventType.LIKE, "video", now, "src:ct1:" + author, now));
    engagementEventRepository.save(
        EngagementEvent.record(author, UUID.randomUUID(), "politician", EngagementEventType.LIKE, "text", now, "src:ct2:" + author, now));

    var byContentType = engagementEventRepository.countByContentType(author, List.of("post_published", "like", "comment"));
    assertThat(byContentType).anySatisfy(tc -> assertThat(tc.key()).isEqualTo("video"));

    var byAccountType = engagementEventRepository.countByActorAccountType(author, List.of("like", "comment", "follow_created"));
    assertThat(byAccountType).anySatisfy(tc -> assertThat(tc.key()).isEqualTo("politician"));
  }

  @Test
  void existsByAuthorAndSourceEventIdIsIdempotencyCheck() {
    UUID author = UUID.randomUUID();
    Instant now = Instant.now();
    String sourceEventId = "src:idem:" + author;

    assertThat(engagementEventRepository.existsByAuthorAndSourceEventId(author, sourceEventId)).isFalse();

    engagementEventRepository.save(
        EngagementEvent.record(author, UUID.randomUUID(), "citizen", EngagementEventType.LIKE, "text", now, sourceEventId, now));

    assertThat(engagementEventRepository.existsByAuthorAndSourceEventId(author, sourceEventId)).isTrue();
  }

  @Test
  void dailyLikeCommentCountsWithinLookbackWindow() {
    UUID author = UUID.randomUUID();
    Instant now = Instant.now();
    engagementEventRepository.save(
        EngagementEvent.record(author, UUID.randomUUID(), "citizen", EngagementEventType.LIKE, "text", now, "src:daily1:" + author, now));

    var daily = engagementEventRepository.dailyLikeCommentCounts(author, now.minusSeconds(3600));
    assertThat(daily).isNotEmpty();
  }
}
