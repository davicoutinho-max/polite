package dev.civicpulse.activityfeed.application;

import dev.civicpulse.activityfeed.application.port.in.GetTimelineUseCase;
import dev.civicpulse.activityfeed.application.port.out.TimelineEventRepository;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class TimelineQueryService implements GetTimelineUseCase {

  private final TimelineEventRepository timelineEventRepository;
  private final Clock clock;

  public TimelineQueryService(TimelineEventRepository timelineEventRepository, Clock clock) {
    this.timelineEventRepository = timelineEventRepository;
    this.clock = clock;
  }

  @Override
  public List<TimelineEntryView> getTimeline(UUID subjectAccountId, int limit) {
    return timelineEventRepository.findBySubject(subjectAccountId, limit).stream().map(event -> TimelineEntryView.of(event, clock)).toList();
  }
}
