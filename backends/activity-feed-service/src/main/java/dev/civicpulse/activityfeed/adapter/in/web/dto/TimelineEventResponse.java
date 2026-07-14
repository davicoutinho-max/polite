package dev.civicpulse.activityfeed.adapter.in.web.dto;

import dev.civicpulse.activityfeed.application.TimelineEntryView;
import java.time.Instant;
import java.util.UUID;

public record TimelineEventResponse(
    UUID id, String type, String title, String detail, Instant occurredAt, String group, UUID actorAccountId, String actorName) {

  public static TimelineEventResponse from(TimelineEntryView view) {
    var event = view.event();
    return new TimelineEventResponse(
        event.id().orElse(null),
        event.type().code(),
        event.title(),
        event.detail().orElse(null),
        event.occurredAt(),
        view.group(),
        event.actorAccountId(),
        event.actorNameDenormalized().orElse(null));
  }
}
