package dev.civicpulse.feedcontent.adapter.out.persistence;

import dev.civicpulse.feedcontent.application.port.out.CommentLikeRepository;
import dev.civicpulse.feedcontent.domain.model.CommentLike;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class CommentLikeRepositoryAdapter implements CommentLikeRepository {

  private final CommentLikeJpaRepository jpaRepository;

  CommentLikeRepositoryAdapter(CommentLikeJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public CommentLike save(CommentLike like) {
    var saved = jpaRepository.save(new CommentLikeJpaEntity(like.commentId(), like.accountId(), like.createdAt()));
    return CommentLike.reconstitute(saved.getCommentId(), saved.getAccountId(), saved.getCreatedAt());
  }

  @Override
  public void delete(UUID commentId, UUID accountId) {
    jpaRepository.deleteById(new CommentLikeId(commentId, accountId));
  }

  @Override
  public boolean exists(UUID commentId, UUID accountId) {
    return jpaRepository.existsById(new CommentLikeId(commentId, accountId));
  }
}
