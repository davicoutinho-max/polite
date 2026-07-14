package dev.civicpulse.feedcontent.application;

import dev.civicpulse.feedcontent.application.port.in.ManageLikeUseCase;
import dev.civicpulse.feedcontent.application.port.out.EventPublisher;
import dev.civicpulse.feedcontent.application.port.out.LikeRepository;
import dev.civicpulse.feedcontent.application.port.out.PostMetricsRepository;
import dev.civicpulse.feedcontent.domain.event.PostLiked;
import dev.civicpulse.feedcontent.domain.exception.AlreadyLikedException;
import dev.civicpulse.feedcontent.domain.exception.PostNotFoundException;
import dev.civicpulse.feedcontent.domain.model.Like;
import dev.civicpulse.feedcontent.domain.model.PostMetrics;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LikeService implements ManageLikeUseCase {

  private final LikeRepository likeRepository;
  private final PostMetricsRepository postMetricsRepository;
  private final EventPublisher eventPublisher;
  private final Clock clock;

  public LikeService(LikeRepository likeRepository, PostMetricsRepository postMetricsRepository, EventPublisher eventPublisher, Clock clock) {
    this.likeRepository = likeRepository;
    this.postMetricsRepository = postMetricsRepository;
    this.eventPublisher = eventPublisher;
    this.clock = clock;
  }

  @Override
  @Transactional
  public void like(UUID postId, UUID accountId) {
    if (likeRepository.exists(postId, accountId)) {
      throw new AlreadyLikedException();
    }
    Instant now = clock.instant();
    likeRepository.save(Like.create(postId, accountId, now));

    PostMetrics metrics = postMetricsRepository.findByPostId(postId).orElseThrow(() -> new PostNotFoundException(postId));
    metrics.incrementLikes(now);
    postMetricsRepository.save(metrics);

    eventPublisher.publish(new PostLiked(postId, accountId, now));
  }

  @Override
  @Transactional
  public void unlike(UUID postId, UUID accountId) {
    if (!likeRepository.exists(postId, accountId)) {
      return; // idempotent
    }
    likeRepository.delete(postId, accountId);

    postMetricsRepository
        .findByPostId(postId)
        .ifPresent(
            metrics -> {
              metrics.decrementLikes(clock.instant());
              postMetricsRepository.save(metrics);
            });
  }
}
