package dev.civicpulse.activityfeed.application;

import static org.assertj.core.api.Assertions.assertThat;

import dev.civicpulse.activityfeed.domain.model.TimelineEvent;
import dev.civicpulse.activityfeed.domain.model.TimelineEventType;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TimelineEntryViewTest {

  private static final Instant NOW = Instant.parse("2026-01-10T12:00:00Z");
  private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

  @Test
  void groupsAsTodayWhenSameDay() {
    TimelineEvent event = eventOccurringAt(NOW.minusSeconds(3600));
    assertThat(TimelineEntryView.of(event, CLOCK).group()).isEqualTo("Today");
  }

  @Test
  void groupsAsYesterdayWhenOneDayBefore() {
    TimelineEvent event = eventOccurringAt(NOW.minus(1, java.time.temporal.ChronoUnit.DAYS));
    assertThat(TimelineEntryView.of(event, CLOCK).group()).isEqualTo("Yesterday");
  }

  @Test
  void groupsAsThisWeekWhenWithinSevenDays() {
    TimelineEvent event = eventOccurringAt(NOW.minus(5, java.time.temporal.ChronoUnit.DAYS));
    assertThat(TimelineEntryView.of(event, CLOCK).group()).isEqualTo("This week");
  }

  @Test
  void groupsAsEarlierWhenBeyondSevenDays() {
    TimelineEvent event = eventOccurringAt(NOW.minus(30, java.time.temporal.ChronoUnit.DAYS));
    assertThat(TimelineEntryView.of(event, CLOCK).group()).isEqualTo("Earlier");
  }

  private static TimelineEvent eventOccurringAt(Instant occurredAt) {
    UUID subject = UUID.randomUUID();
    return TimelineEvent.record(subject, TimelineEventType.VOTE, "Voted YES", null, occurredAt, "src:" + occurredAt, subject, null, NOW);
  }
}
