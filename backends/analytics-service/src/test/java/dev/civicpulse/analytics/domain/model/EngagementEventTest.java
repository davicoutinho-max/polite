package dev.civicpulse.analytics.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class EngagementEventTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Test
  void recordCreatesAnEventWithNoIdYet() {
    UUID author = UUID.randomUUID();
    UUID actor = UUID.randomUUID();
    EngagementEvent event = EngagementEvent.record(author, actor, "citizen", EngagementEventType.LIKE, "text", NOW, "post-liked:x", NOW);

    assertThat(event.id()).isEmpty();
    assertThat(event.authorAccountId()).isEqualTo(author);
    assertThat(event.actorAccountType()).contains("citizen");
  }

  @Test
  void blankSourceEventIdIsRejected() {
    UUID author = UUID.randomUUID();
    UUID actor = UUID.randomUUID();
    assertThatThrownBy(() -> EngagementEvent.record(author, actor, null, EngagementEventType.LIKE, null, NOW, "  ", NOW))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
