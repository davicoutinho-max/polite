package dev.civicpulse.feedcontent.adapter.out.persistence;

import dev.civicpulse.feedcontent.application.port.out.CommentRepository;
import dev.civicpulse.feedcontent.domain.model.Comment;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class CommentRepositoryAdapter implements CommentRepository {

  private final CommentJpaRepository jpaRepository;

  CommentRepositoryAdapter(CommentJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public Comment save(Comment comment) {
    var saved =
        jpaRepository.save(
            new CommentJpaEntity(
                comment.id(),
                comment.postId(),
                comment.authorAccountId(),
                comment.parentCommentId(),
                comment.body(),
                comment.createdAt(),
                comment.likesCount()));
    return toDomain(saved);
  }

  @Override
  public Optional<Comment> findById(UUID commentId) {
    return jpaRepository.findById(commentId).map(CommentRepositoryAdapter::toDomain);
  }

  @Override
  public List<Comment> findByPostId(UUID postId) {
    return jpaRepository.findByPostIdOrderByCreatedAtAsc(postId).stream().map(CommentRepositoryAdapter::toDomain).toList();
  }

  @Override
  public void deleteByPostId(UUID postId) {
    jpaRepository.deleteByPostId(postId);
  }

  private static Comment toDomain(CommentJpaEntity entity) {
    return Comment.reconstitute(
        entity.getId(),
        entity.getPostId(),
        entity.getAuthorAccountId(),
        entity.getParentCommentId(),
        entity.getBody(),
        entity.getCreatedAt(),
        entity.getLikesCount());
  }
}
