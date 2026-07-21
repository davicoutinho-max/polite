package dev.civicpulse.feedcontent.adapter.out.persistence;

import dev.civicpulse.feedcontent.application.port.out.PostMetricsRepository;
import dev.civicpulse.feedcontent.domain.model.PostMetrics;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class PostMetricsRepositoryAdapter implements PostMetricsRepository {

  private final PostMetricsJpaRepository jpaRepository;

  PostMetricsRepositoryAdapter(PostMetricsJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public PostMetrics save(PostMetrics metrics) {
    var saved =
        jpaRepository.save(
            new PostMetricsJpaEntity(metrics.postId(), metrics.likesCount(), metrics.commentsCount(), metrics.sharesCount(), metrics.updatedAt()));
    return toDomain(saved);
  }

  @Override
  public Optional<PostMetrics> findByPostId(UUID postId) {
    return jpaRepository.findById(postId).map(PostMetricsRepositoryAdapter::toDomain);
  }

  @Override
  public void deleteByPostId(UUID postId) {
    jpaRepository.deleteById(postId);
  }

  private static PostMetrics toDomain(PostMetricsJpaEntity entity) {
    return PostMetrics.reconstitute(entity.getPostId(), entity.getLikesCount(), entity.getCommentsCount(), entity.getSharesCount(), entity.getUpdatedAt());
  }
}
