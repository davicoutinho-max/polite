package dev.civicpulse.analytics.adapter.out.persistence;

import dev.civicpulse.analytics.domain.model.EngagementEventType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface EngagementEventJpaRepository extends JpaRepository<EngagementEventJpaEntity, Long> {

  boolean existsByAuthorAccountIdAndSourceEventId(UUID authorAccountId, String sourceEventId);

  long countByAuthorAccountIdAndEventType(UUID authorAccountId, EngagementEventType eventType);

  @Query("select count(distinct e.actorAccountId) from EngagementEventJpaEntity e where e.authorAccountId = :authorAccountId and e.eventType in :eventTypes")
  long countDistinctActors(@Param("authorAccountId") UUID authorAccountId, @Param("eventTypes") List<EngagementEventType> eventTypes);

  @Query(
      value =
          "select date_trunc('day', occurred_at)::date as day, "
              + "count(*) filter (where event_type = 'like') as likes, "
              + "count(*) filter (where event_type = 'comment') as comments "
              + "from engagement_events where author_account_id = :authorAccountId and occurred_at >= :since "
              + "group by day order by day",
      nativeQuery = true)
  List<DailyCountProjection> dailyLikeCommentCounts(@Param("authorAccountId") UUID authorAccountId, @Param("since") Instant since);

  @Query(
      "select new dev.civicpulse.analytics.adapter.out.persistence.TypeCountProjection(e.contentType, count(e)) from EngagementEventJpaEntity e "
          + "where e.authorAccountId = :authorAccountId and e.eventType in :eventTypes group by e.contentType")
  List<TypeCountProjection> countByContentType(@Param("authorAccountId") UUID authorAccountId, @Param("eventTypes") List<EngagementEventType> eventTypes);

  @Query(
      "select new dev.civicpulse.analytics.adapter.out.persistence.TypeCountProjection(e.actorAccountType, count(e)) from EngagementEventJpaEntity e "
          + "where e.authorAccountId = :authorAccountId and e.eventType in :eventTypes group by e.actorAccountType")
  List<TypeCountProjection> countByActorAccountType(
      @Param("authorAccountId") UUID authorAccountId, @Param("eventTypes") List<EngagementEventType> eventTypes);
}
