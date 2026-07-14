package dev.civicpulse.feedcontent.adapter.in.web.dto;

import dev.civicpulse.feedcontent.domain.model.PostMetrics;
import java.time.Instant;
import java.util.UUID;

public record PostMetricsResponse(UUID postId, int likesCount, int commentsCount, int sharesCount, Instant updatedAt) {

  public static PostMetricsResponse from(PostMetrics metrics) {
    return new PostMetricsResponse(
        metrics.postId(), metrics.likesCount(), metrics.commentsCount(), metrics.sharesCount(), metrics.updatedAt());
  }
}
