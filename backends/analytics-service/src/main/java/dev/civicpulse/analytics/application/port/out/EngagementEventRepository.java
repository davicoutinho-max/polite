package dev.civicpulse.analytics.application.port.out;

import dev.civicpulse.analytics.domain.model.EngagementEvent;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface EngagementEventRepository {

  EngagementEvent save(EngagementEvent event);

  boolean existsByAuthorAndSourceEventId(UUID authorAccountId, String sourceEventId);

  long countByAuthorAndType(UUID authorAccountId, String eventType);

  long countDistinctActors(UUID authorAccountId, List<String> eventTypes);

  List<DailyCount> dailyLikeCommentCounts(UUID authorAccountId, Instant since);

  List<TypeCount> countByContentType(UUID authorAccountId, List<String> eventTypes);

  List<TypeCount> countByActorAccountType(UUID authorAccountId, List<String> eventTypes);

  record DailyCount(LocalDate day, long likes, long comments) {}

  record TypeCount(String key, long count) {}
}
