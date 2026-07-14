package dev.civicpulse.feedcontent.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PostMetricsTest {

  private static final Instant T0 = Instant.parse("2026-01-01T00:00:00Z");
  private static final Instant T1 = Instant.parse("2026-01-01T00:05:00Z");

  @Test
  void initialMetricsStartAtZero() {
    PostMetrics metrics = PostMetrics.initial(UUID.randomUUID(), T0);

    assertThat(metrics.likesCount()).isZero();
    assertThat(metrics.commentsCount()).isZero();
    assertThat(metrics.sharesCount()).isZero();
    assertThat(metrics.updatedAt()).isEqualTo(T0);
  }

  @Test
  void incrementLikesIncreasesCountAndTouchesUpdatedAt() {
    PostMetrics metrics = PostMetrics.initial(UUID.randomUUID(), T0);

    metrics.incrementLikes(T1);

    assertThat(metrics.likesCount()).isEqualTo(1);
    assertThat(metrics.updatedAt()).isEqualTo(T1);
  }

  @Test
  void decrementLikesFloorsAtZero() {
    PostMetrics metrics = PostMetrics.initial(UUID.randomUUID(), T0);

    metrics.decrementLikes(T1);

    assertThat(metrics.likesCount()).isZero();
  }

  @Test
  void decrementLikesAfterIncrementReturnsToZero() {
    PostMetrics metrics = PostMetrics.initial(UUID.randomUUID(), T0);
    metrics.incrementLikes(T0);
    metrics.incrementLikes(T0);

    metrics.decrementLikes(T1);

    assertThat(metrics.likesCount()).isEqualTo(1);
  }

  @Test
  void incrementCommentsIncreasesCount() {
    PostMetrics metrics = PostMetrics.initial(UUID.randomUUID(), T0);

    metrics.incrementComments(T1);
    metrics.incrementComments(T1);

    assertThat(metrics.commentsCount()).isEqualTo(2);
  }
}
