package dev.civicpulse.feedcontent.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.civicpulse.feedcontent.application.port.out.CommentRepository;
import dev.civicpulse.feedcontent.application.port.out.EventPublisher;
import dev.civicpulse.feedcontent.application.port.out.PostMetricsRepository;
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
class CommentServiceTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Mock private CommentRepository commentRepository;
  @Mock private PostMetricsRepository postMetricsRepository;
  @Mock private EventPublisher eventPublisher;

  private CommentService service;

  @BeforeEach
  void setUp() {
    service = new CommentService(commentRepository, postMetricsRepository, eventPublisher, Clock.fixed(NOW, ZoneOffset.UTC));
  }

  @Test
  void addCommentIncrementsMetricsAndPublishesEvent() {
    when(commentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    UUID postId = UUID.randomUUID();
    when(postMetricsRepository.findByPostId(postId)).thenReturn(Optional.of(PostMetrics.initial(postId, NOW)));

    var comment = service.addComment(postId, UUID.randomUUID(), "great post");

    assertThat(comment.body()).isEqualTo("great post");
    verify(postMetricsRepository).save(any());
    verify(eventPublisher).publish(any());
  }

  @Test
  void addCommentThrowsPostNotFoundWhenMetricsMissing() {
    UUID postId = UUID.randomUUID();
    when(postMetricsRepository.findByPostId(postId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.addComment(postId, UUID.randomUUID(), "body")).isInstanceOf(PostNotFoundException.class);
  }

  @Test
  void listByPostDelegatesToRepository() {
    UUID postId = UUID.randomUUID();

    service.listByPost(postId);

    verify(commentRepository).findByPostId(postId);
  }
}
