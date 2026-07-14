package dev.civicpulse.directory.adapter.out.persistence;

import dev.civicpulse.directory.application.port.out.FollowRepository;
import dev.civicpulse.directory.domain.model.Follow;
import dev.civicpulse.directory.domain.model.FollowTargetType;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class FollowRepositoryAdapter implements FollowRepository {

  private final FollowJpaRepository jpaRepository;

  FollowRepositoryAdapter(FollowJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public Follow save(Follow follow) {
    var saved =
        jpaRepository.save(
            new FollowJpaEntity(follow.followerAccountId(), follow.targetType().code(), follow.targetId(), follow.createdAt()));
    return toDomain(saved);
  }

  @Override
  public void delete(UUID followerAccountId, FollowTargetType targetType, UUID targetId) {
    jpaRepository.deleteById(new FollowId(followerAccountId, targetType.code(), targetId));
  }

  @Override
  public boolean exists(UUID followerAccountId, FollowTargetType targetType, UUID targetId) {
    return jpaRepository.existsById(new FollowId(followerAccountId, targetType.code(), targetId));
  }

  @Override
  public List<Follow> findByFollower(UUID followerAccountId) {
    return jpaRepository.findByFollowerAccountId(followerAccountId).stream().map(FollowRepositoryAdapter::toDomain).toList();
  }

  private static Follow toDomain(FollowJpaEntity entity) {
    return Follow.reconstitute(
        entity.getFollowerAccountId(), FollowTargetType.fromCode(entity.getTargetType()), entity.getTargetId(), entity.getCreatedAt());
  }
}
