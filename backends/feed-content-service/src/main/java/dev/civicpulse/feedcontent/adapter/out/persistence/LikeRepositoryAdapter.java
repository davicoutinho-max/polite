package dev.civicpulse.feedcontent.adapter.out.persistence;

import dev.civicpulse.feedcontent.application.port.out.LikeRepository;
import dev.civicpulse.feedcontent.domain.model.Like;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class LikeRepositoryAdapter implements LikeRepository {

  private final LikeJpaRepository jpaRepository;

  LikeRepositoryAdapter(LikeJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public Like save(Like like) {
    var saved = jpaRepository.save(new LikeJpaEntity(like.postId(), like.accountId(), like.createdAt()));
    return Like.reconstitute(saved.getPostId(), saved.getAccountId(), saved.getCreatedAt());
  }

  @Override
  public void delete(UUID postId, UUID accountId) {
    jpaRepository.deleteById(new LikeId(postId, accountId));
  }

  @Override
  public void deleteByPostId(UUID postId) {
    jpaRepository.deleteByPostId(postId);
  }

  @Override
  public boolean exists(UUID postId, UUID accountId) {
    return jpaRepository.existsById(new LikeId(postId, accountId));
  }
}
