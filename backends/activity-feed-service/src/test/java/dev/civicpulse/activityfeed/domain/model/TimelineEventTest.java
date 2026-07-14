package dev.civicpulse.activityfeed.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TimelineEventTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Test
  void recordCreatesAnEventWithNoIdYet() {
    UUID subject = UUID.randomUUID();
    TimelineEvent event =
        TimelineEvent.record(subject, TimelineEventType.VOTE, "Voted YES", null, NOW, "vote-cast:abc", subject, "Jane Doe", NOW);

    assertThat(event.id()).isEmpty();
    assertThat(event.subjectAccountId()).isEqualTo(subject);
    assertThat(event.actorNameDenormalized()).contains("Jane Doe");
  }

  @Test
  void blankTitleIsRejected() {
    UUID subject = UUID.randomUUID();
    assertThatThrownBy(() -> TimelineEvent.record(subject, TimelineEventType.VOTE, "  ", null, NOW, "src", subject, null, NOW))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void typeFromLegislativeItemCategoryMapsKnownCodes() {
    assertThat(TimelineEventType.fromLegislativeItemCategory("project")).isEqualTo(TimelineEventType.PROJECT);
    assertThat(TimelineEventType.fromLegislativeItemCategory("pec")).isEqualTo(TimelineEventType.PEC);
    assertThat(TimelineEventType.fromLegislativeItemCategory("cpi")).isEqualTo(TimelineEventType.CPI);
  }
}
