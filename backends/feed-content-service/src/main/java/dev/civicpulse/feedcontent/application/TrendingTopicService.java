package dev.civicpulse.feedcontent.application;

import dev.civicpulse.feedcontent.application.port.in.GetTrendingTopicsUseCase;
import dev.civicpulse.feedcontent.application.port.in.RecomputeTrendingTopicsUseCase;
import dev.civicpulse.feedcontent.application.port.out.PostHashtagRepository;
import dev.civicpulse.feedcontent.application.port.out.TrendingTopicCacheRepository;
import dev.civicpulse.feedcontent.domain.model.TrendingTopic;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TrendingTopicService implements GetTrendingTopicsUseCase, RecomputeTrendingTopicsUseCase {

  private static final int CACHE_SIZE = 10;

  private final PostHashtagRepository postHashtagRepository;
  private final TrendingTopicCacheRepository trendingTopicCacheRepository;
  private final Clock clock;

  public TrendingTopicService(
      PostHashtagRepository postHashtagRepository, TrendingTopicCacheRepository trendingTopicCacheRepository, Clock clock) {
    this.postHashtagRepository = postHashtagRepository;
    this.trendingTopicCacheRepository = trendingTopicCacheRepository;
    this.clock = clock;
  }

  @Override
  public List<TrendingTopic> getTrending(int limit) {
    return trendingTopicCacheRepository.findTopRanked(limit);
  }

  @Override
  @Transactional
  public void recompute() {
    Instant now = clock.instant();
    Instant since = now.minus(Duration.ofHours(24));
    List<PostHashtagRepository.HashtagCount> counts = postHashtagRepository.countByHashtagSince(since, CACHE_SIZE);

    List<TrendingTopic> topics = new ArrayList<>();
    short rank = 1;
    for (PostHashtagRepository.HashtagCount count : counts) {
      topics.add(new TrendingTopic(count.hashtag(), count.count(), rank, now));
      rank++;
    }
    trendingTopicCacheRepository.replaceAll(topics);
  }
}
