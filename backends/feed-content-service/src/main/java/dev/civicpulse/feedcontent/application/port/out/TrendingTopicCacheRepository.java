package dev.civicpulse.feedcontent.application.port.out;

import dev.civicpulse.feedcontent.domain.model.TrendingTopic;
import java.util.List;

public interface TrendingTopicCacheRepository {

  void replaceAll(List<TrendingTopic> topics);

  List<TrendingTopic> findTopRanked(int limit);
}
