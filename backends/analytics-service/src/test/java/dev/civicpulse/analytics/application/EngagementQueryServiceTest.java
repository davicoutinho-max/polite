package dev.civicpulse.analytics.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.when;

import dev.civicpulse.analytics.application.port.out.EngagementEventRepository;
import dev.civicpulse.analytics.domain.model.EngagementEventType;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EngagementQueryServiceTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Mock private EngagementEventRepository engagementEventRepository;

  private EngagementQueryService service;

  @BeforeEach
  void setUp() {
    service = new EngagementQueryService(engagementEventRepository, Clock.fixed(NOW, ZoneOffset.UTC));
  }

  @Test
  void kpisComputeReachAndEngagementRate() {
    UUID authorId = UUID.randomUUID();
    when(engagementEventRepository.countByAuthorAndType(authorId, EngagementEventType.POST_PUBLISHED.code())).thenReturn(5L);
    when(engagementEventRepository.countByAuthorAndType(authorId, EngagementEventType.LIKE.code())).thenReturn(30L);
    when(engagementEventRepository.countByAuthorAndType(authorId, EngagementEventType.COMMENT.code())).thenReturn(10L);
    when(engagementEventRepository.countByAuthorAndType(authorId, EngagementEventType.FOLLOW_CREATED.code())).thenReturn(8L);
    when(engagementEventRepository.countByAuthorAndType(authorId, EngagementEventType.FOLLOW_REMOVED.code())).thenReturn(3L);
    when(engagementEventRepository.countDistinctActors(authorId, List.of("like", "comment", "follow_created"))).thenReturn(20L);

    KpiSummary summary = service.getKpis(authorId);

    assertThat(summary.totalPosts()).isEqualTo(5L);
    assertThat(summary.netFollows()).isEqualTo(5L);
    assertThat(summary.reach()).isEqualTo(20L);
    assertThat(summary.engagementRatePercent()).isCloseTo(200.0, within(0.001));
  }
}
