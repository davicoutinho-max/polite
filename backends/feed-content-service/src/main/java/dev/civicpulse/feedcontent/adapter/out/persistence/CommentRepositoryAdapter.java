package dev.civicpulse.feedcontent.adapter.out.persistence;

import dev.civicpulse.feedcontent.application.port.out.CommentRepository;
import dev.civicpulse.feedcontent.domain.model.Comment;
import java.util.List;
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
        jpaRepository.save(new CommentJpaEntity(comment.id(), comment.postId(), comment.authorAccountId(), comment.body(), comment.createdAt()));
    return toDomain(saved);
  }

  @Override
  public List<Comment> findByPostId(UUID postId) {
    return jpaRepository.findByPostIdOrderByCreatedAtAsc(postId).stream().map(CommentRepositoryAdapter::toDomain).toList();
  }

  private static Comment toDomain(CommentJpaEntity entity) {
    return Comment.reconstitute(entity.getId(), entity.getPostId(), entity.getAuthorAccountId(), entity.getBody(), entity.getCreatedAt());
  }
}
