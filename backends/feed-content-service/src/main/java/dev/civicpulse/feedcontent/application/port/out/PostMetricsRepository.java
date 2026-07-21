package dev.civicpulse.feedcontent.application.port.out;

import dev.civicpulse.feedcontent.domain.model.PostMetrics;
import java.util.Optional;
import java.util.UUID;

public interface PostMetricsRepository {

  PostMetrics save(PostMetrics metrics);

  Optional<PostMetrics> findByPostId(UUID postId);

  void deleteByPostId(UUID postId);
}
