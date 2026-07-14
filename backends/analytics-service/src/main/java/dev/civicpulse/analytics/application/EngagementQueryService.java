package dev.civicpulse.analytics.application;

import dev.civicpulse.analytics.application.port.in.GetAnalyticsUseCase;
import dev.civicpulse.analytics.application.port.out.EngagementEventRepository;
import dev.civicpulse.analytics.application.port.out.EngagementEventRepository.DailyCount;
import dev.civicpulse.analytics.application.port.out.EngagementEventRepository.TypeCount;
import dev.civicpulse.analytics.domain.model.EngagementEventType;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class EngagementQueryService implements GetAnalyticsUseCase {

  private static final List<String> REACH_EVENT_TYPES =
      List.of(EngagementEventType.LIKE.code(), EngagementEventType.COMMENT.code(), EngagementEventType.FOLLOW_CREATED.code());
  private static final List<String> CONTENT_EVENT_TYPES =
      List.of(EngagementEventType.POST_PUBLISHED.code(), EngagementEventType.LIKE.code(), EngagementEventType.COMMENT.code());

  private final EngagementEventRepository engagementEventRepository;
  private final Clock clock;

  public EngagementQueryService(EngagementEventRepository engagementEventRepository, Clock clock) {
    this.engagementEventRepository = engagementEventRepository;
    this.clock = clock;
  }

  @Override
  public KpiSummary getKpis(UUID authorAccountId) {
    long totalPosts = engagementEventRepository.countByAuthorAndType(authorAccountId, EngagementEventType.POST_PUBLISHED.code());
    long totalLikes = engagementEventRepository.countByAuthorAndType(authorAccountId, EngagementEventType.LIKE.code());
    long totalComments = engagementEventRepository.countByAuthorAndType(authorAccountId, EngagementEventType.COMMENT.code());
    long followsGained = engagementEventRepository.countByAuthorAndType(authorAccountId, EngagementEventType.FOLLOW_CREATED.code());
    long followsLost = engagementEventRepository.countByAuthorAndType(authorAccountId, EngagementEventType.FOLLOW_REMOVED.code());
    long reach = engagementEventRepository.countDistinctActors(authorAccountId, REACH_EVENT_TYPES);
    return KpiSummary.of(totalPosts, totalLikes, totalComments, followsGained - followsLost, reach);
  }

  @Override
  public List<DailyCount> getEngagementTrend(UUID authorAccountId, int lookbackDays) {
    Instant since = clock.instant().minus(Duration.ofDays(lookbackDays));
    return engagementEventRepository.dailyLikeCommentCounts(authorAccountId, since);
  }

  @Override
  public List<TypeCount> getByContentType(UUID authorAccountId) {
    return engagementEventRepository.countByContentType(authorAccountId, CONTENT_EVENT_TYPES);
  }

  @Override
  public List<TypeCount> getByAccountType(UUID authorAccountId) {
    return engagementEventRepository.countByActorAccountType(authorAccountId, REACH_EVENT_TYPES);
  }
}
