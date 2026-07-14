package dev.civicpulse.feedcontent.adapter.in.web.dto;

import dev.civicpulse.feedcontent.domain.model.TrendingTopic;

public record TrendingTopicResponse(String hashtag, long postCountLast24h, short rank) {

  public static TrendingTopicResponse from(TrendingTopic topic) {
    return new TrendingTopicResponse(topic.hashtag(), topic.postCountLast24h(), topic.rank());
  }
}
