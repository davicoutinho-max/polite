package dev.civicpulse.activityfeed.application;

import dev.civicpulse.activityfeed.domain.model.TimelineEvent;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

/** {@code group} (Today/Yesterday/This week/Earlier) is computed here, at read time, from
 * {@code occurredAt} — never stored, see schema.sql's table comment. */
public record TimelineEntryView(TimelineEvent event, String group) {

  public static TimelineEntryView of(TimelineEvent event, Clock clock) {
    return new TimelineEntryView(event, computeGroup(event.occurredAt(), clock));
  }

  private static String computeGroup(Instant occurredAt, Clock clock) {
    LocalDate today = LocalDate.now(clock.withZone(ZoneOffset.UTC));
    LocalDate occurredDate = occurredAt.atZone(ZoneOffset.UTC).toLocalDate();
    long daysBetween = ChronoUnit.DAYS.between(occurredDate, today);
    if (daysBetween == 0) {
      return "Today";
    }
    if (daysBetween == 1) {
      return "Yesterday";
    }
    if (daysBetween <= 7) {
      return "This week";
    }
    return "Earlier";
  }
}
