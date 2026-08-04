package dev.civicpulse.feedcontent.application;

import dev.civicpulse.feedcontent.application.port.in.ManageCommentLikeUseCase;
import dev.civicpulse.feedcontent.application.port.out.CommentLikeRepository;
import dev.civicpulse.feedcontent.application.port.out.CommentRepository;
import dev.civicpulse.feedcontent.domain.exception.AlreadyLikedException;
import dev.civicpulse.feedcontent.domain.exception.CommentNotFoundException;
import dev.civicpulse.feedcontent.domain.model.Comment;
import dev.civicpulse.feedcontent.domain.model.CommentLike;
import java.time.Clock;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Mirrors {@link LikeService} (post likes), but the like count lives directly on the comment row
 * instead of a separate metrics table — comments never had a metrics concept, so adding one just
 * for this would be more machinery than the feature needs. */
@Service
public class CommentLikeService implements ManageCommentLikeUseCase {

  private final CommentLikeRepository commentLikeRepository;
  private final CommentRepository commentRepository;
  private final Clock clock;

  public CommentLikeService(CommentLikeRepository commentLikeRepository, CommentRepository commentRepository, Clock clock) {
    this.commentLikeRepository = commentLikeRepository;
    this.commentRepository = commentRepository;
    this.clock = clock;
  }

  @Override
  @Transactional
  public void like(UUID commentId, UUID accountId) {
    if (commentLikeRepository.exists(commentId, accountId)) {
      throw new AlreadyLikedException();
    }
    Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new CommentNotFoundException(commentId));
    commentLikeRepository.save(CommentLike.create(commentId, accountId, clock.instant()));
    comment.incrementLikes();
    commentRepository.save(comment);
  }

  @Override
  @Transactional
  public void unlike(UUID commentId, UUID accountId) {
    if (!commentLikeRepository.exists(commentId, accountId)) {
      return; // idempotent
    }
    commentLikeRepository.delete(commentId, accountId);
    commentRepository
        .findById(commentId)
        .ifPresent(
            comment -> {
              comment.decrementLikes();
              commentRepository.save(comment);
            });
  }

  @Override
  @Transactional(readOnly = true)
  public boolean isLiked(UUID commentId, UUID accountId) {
    return commentLikeRepository.exists(commentId, accountId);
  }
}
