package dev.civicpulse.feedcontent.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.civicpulse.feedcontent.application.port.out.EventPublisher;
import dev.civicpulse.feedcontent.application.port.out.LikeRepository;
import dev.civicpulse.feedcontent.application.port.out.PostMetricsRepository;
import dev.civicpulse.feedcontent.domain.exception.AlreadyLikedException;
import dev.civicpulse.feedcontent.domain.exception.PostNotFoundException;
import dev.civicpulse.feedcontent.domain.model.PostMetrics;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Mock private LikeRepository likeRepository;
  @Mock private PostMetricsRepository postMetricsRepository;
  @Mock private EventPublisher eventPublisher;

  private LikeService service;

  @BeforeEach
  void setUp() {
    service = new LikeService(likeRepository, postMetricsRepository, eventPublisher, Clock.fixed(NOW, ZoneOffset.UTC));
  }

  @Test
  void likeIncrementsMetricsAndPublishesEvent() {
    UUID postId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    when(likeRepository.exists(postId, accountId)).thenReturn(false);
    when(postMetricsRepository.findByPostId(postId)).thenReturn(Optional.of(PostMetrics.initial(postId, NOW)));

    service.like(postId, accountId);

    verify(likeRepository).save(any());
    verify(postMetricsRepository).save(any());
    verify(eventPublisher).publish(any());
  }

  @Test
  void likeThrowsWhenAlreadyLiked() {
    UUID postId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    when(likeRepository.exists(postId, accountId)).thenReturn(true);

    assertThatThrownBy(() -> service.like(postId, accountId)).isInstanceOf(AlreadyLikedException.class);
    verify(likeRepository, never()).save(any());
  }

  @Test
  void likeThrowsPostNotFoundWhenMetricsMissing() {
    UUID postId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    when(likeRepository.exists(postId, accountId)).thenReturn(false);
    when(postMetricsRepository.findByPostId(postId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.like(postId, accountId)).isInstanceOf(PostNotFoundException.class);
  }

  @Test
  void unlikeIsIdempotentWhenNotCurrentlyLiked() {
    UUID postId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    when(likeRepository.exists(postId, accountId)).thenReturn(false);

    service.unlike(postId, accountId);

    verify(likeRepository, never()).delete(any(), any());
  }

  @Test
  void unlikeDecrementsMetricsWhenCurrentlyLiked() {
    UUID postId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    PostMetrics metrics = PostMetrics.initial(postId, NOW);
    metrics.incrementLikes(NOW);
    when(likeRepository.exists(postId, accountId)).thenReturn(true);
    when(postMetricsRepository.findByPostId(postId)).thenReturn(Optional.of(metrics));

    service.unlike(postId, accountId);

    verify(likeRepository).delete(postId, accountId);
    assertThat(metrics.likesCount()).isZero();
  }

  @Test
  void isLikedDelegatesToRepository() {
    UUID postId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    when(likeRepository.exists(postId, accountId)).thenReturn(true);

    assertThat(service.isLiked(postId, accountId)).isTrue();
  }
}
