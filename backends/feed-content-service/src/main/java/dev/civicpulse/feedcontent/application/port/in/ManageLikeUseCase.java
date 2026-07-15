package dev.civicpulse.feedcontent.application.port.in;

import java.util.UUID;

public interface ManageLikeUseCase {

  void like(UUID postId, UUID accountId);

  void unlike(UUID postId, UUID accountId);

  boolean isLiked(UUID postId, UUID accountId);
}
