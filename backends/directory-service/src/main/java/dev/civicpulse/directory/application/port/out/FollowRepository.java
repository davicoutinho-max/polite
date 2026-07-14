package dev.civicpulse.directory.application.port.out;

import dev.civicpulse.directory.domain.model.Follow;
import dev.civicpulse.directory.domain.model.FollowTargetType;
import java.util.List;
import java.util.UUID;

public interface FollowRepository {

  Follow save(Follow follow);

  void delete(UUID followerAccountId, FollowTargetType targetType, UUID targetId);

  boolean exists(UUID followerAccountId, FollowTargetType targetType, UUID targetId);

  List<Follow> findByFollower(UUID followerAccountId);
}
