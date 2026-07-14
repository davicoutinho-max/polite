package dev.civicpulse.activityfeed.application.port.out;

import dev.civicpulse.activityfeed.domain.model.TimelineEvent;
import java.util.List;
import java.util.UUID;

public interface TimelineEventRepository {

  TimelineEvent save(TimelineEvent event);

  boolean existsBySubjectAndSourceEventId(UUID subjectAccountId, String sourceEventId);

  List<TimelineEvent> findBySubject(UUID subjectAccountId, int limit);
}
