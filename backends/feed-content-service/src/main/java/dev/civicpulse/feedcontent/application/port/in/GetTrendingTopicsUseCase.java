package dev.civicpulse.feedcontent.application.port.in;

import dev.civicpulse.feedcontent.domain.model.TrendingTopic;
import java.util.List;

public interface GetTrendingTopicsUseCase {

  List<TrendingTopic> getTrending(int limit);
}
