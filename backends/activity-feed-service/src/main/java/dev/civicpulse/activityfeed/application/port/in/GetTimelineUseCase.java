package dev.civicpulse.activityfeed.application.port.in;

import dev.civicpulse.activityfeed.application.TimelineEntryView;
import java.util.List;
import java.util.UUID;

public interface GetTimelineUseCase {

  List<TimelineEntryView> getTimeline(UUID subjectAccountId, int limit);
}
