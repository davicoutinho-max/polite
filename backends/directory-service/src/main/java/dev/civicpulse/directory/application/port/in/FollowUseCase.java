package dev.civicpulse.directory.application.port.in;

import dev.civicpulse.directory.domain.model.FollowTargetType;
import java.util.List;
import java.util.UUID;

public interface FollowUseCase {

  void follow(UUID followerAccountId, FollowTargetType targetType, UUID targetId);

  void unfollow(UUID followerAccountId, FollowTargetType targetType, UUID targetId);

  boolean isFollowing(UUID followerAccountId, FollowTargetType targetType, UUID targetId);

  List<UUID> listFollowingTargets(UUID followerAccountId, FollowTargetType targetType);
}
