package dev.civicpulse.feedcontent.application;

import dev.civicpulse.feedcontent.application.port.in.ManageCommentUseCase;
import dev.civicpulse.feedcontent.application.port.out.CommentRepository;
import dev.civicpulse.feedcontent.application.port.out.EventPublisher;
import dev.civicpulse.feedcontent.application.port.out.PostMetricsRepository;
import dev.civicpulse.feedcontent.domain.event.CommentAdded;
import dev.civicpulse.feedcontent.domain.exception.CommentNotFoundException;
import dev.civicpulse.feedcontent.domain.exception.PostNotFoundException;
import dev.civicpulse.feedcontent.domain.model.Comment;
import dev.civicpulse.feedcontent.domain.model.PostMetrics;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentService implements ManageCommentUseCase {

  private final CommentRepository commentRepository;
  private final PostMetricsRepository postMetricsRepository;
  private final EventPublisher eventPublisher;
  private final Clock clock;

  public CommentService(
      CommentRepository commentRepository, PostMetricsRepository postMetricsRepository, EventPublisher eventPublisher, Clock clock) {
    this.commentRepository = commentRepository;
    this.postMetricsRepository = postMetricsRepository;
    this.eventPublisher = eventPublisher;
    this.clock = clock;
  }

  @Override
  @Transactional
  public Comment addComment(UUID postId, UUID authorAccountId, UUID parentCommentId, String body) {
    Instant now = clock.instant();
    UUID resolvedParentId = resolveParentId(parentCommentId);
    Comment comment = commentRepository.save(Comment.add(UUID.randomUUID(), postId, authorAccountId, resolvedParentId, body, now));

    PostMetrics metrics = postMetricsRepository.findByPostId(postId).orElseThrow(() -> new PostNotFoundException(postId));
    metrics.incrementComments(now);
    postMetricsRepository.save(metrics);

    eventPublisher.publish(new CommentAdded(postId, comment.id(), now));
    return comment;
  }

  /** A reply to a reply flattens to the original top-level comment, so threads never nest past
   * one level — see ManageCommentUseCase's javadoc. */
  private UUID resolveParentId(UUID parentCommentId) {
    if (parentCommentId == null) {
      return null;
    }
    Comment parent = commentRepository.findById(parentCommentId).orElseThrow(() -> new CommentNotFoundException(parentCommentId));
    return parent.parentCommentId() != null ? parent.parentCommentId() : parent.id();
  }

  @Override
  @Transactional(readOnly = true)
  public List<Comment> listByPost(UUID postId) {
    return commentRepository.findByPostId(postId);
  }
}
