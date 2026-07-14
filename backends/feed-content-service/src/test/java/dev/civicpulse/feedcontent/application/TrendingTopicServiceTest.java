package dev.civicpulse.feedcontent.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.civicpulse.feedcontent.application.port.out.PostHashtagRepository;
import dev.civicpulse.feedcontent.application.port.out.PostHashtagRepository.HashtagCount;
import dev.civicpulse.feedcontent.application.port.out.TrendingTopicCacheRepository;
import dev.civicpulse.feedcontent.domain.model.TrendingTopic;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrendingTopicServiceTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Mock private PostHashtagRepository postHashtagRepository;
  @Mock private TrendingTopicCacheRepository trendingTopicCacheRepository;

  private TrendingTopicService service;

  @BeforeEach
  void setUp() {
    service = new TrendingTopicService(postHashtagRepository, trendingTopicCacheRepository, Clock.fixed(NOW, ZoneOffset.UTC));
  }

  @Test
  void recomputeRanksHashtagsByCountDescending() {
    when(postHashtagRepository.countByHashtagSince(any(), anyInt()))
        .thenReturn(List.of(new HashtagCount("civic", 42), new HashtagCount("cleanwater", 17)));

    service.recompute();

    ArgumentCaptor<List<TrendingTopic>> captor = ArgumentCaptor.forClass(List.class);
    verify(trendingTopicCacheRepository).replaceAll(captor.capture());
    List<TrendingTopic> topics = captor.getValue();
    assertThat(topics).hasSize(2);
    assertThat(topics.get(0).hashtag()).isEqualTo("civic");
    assertThat(topics.get(0).rank()).isEqualTo((short) 1);
    assertThat(topics.get(1).hashtag()).isEqualTo("cleanwater");
    assertThat(topics.get(1).rank()).isEqualTo((short) 2);
  }

  @Test
  void getTrendingDelegatesToCache() {
    when(trendingTopicCacheRepository.findTopRanked(5)).thenReturn(List.of(new TrendingTopic("civic", 42, (short) 1, NOW)));

    List<TrendingTopic> result = service.getTrending(5);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).hashtag()).isEqualTo("civic");
  }
}
