package dev.civicpulse.feedcontent.application.port.in;

import java.util.UUID;

public interface ManageCommentLikeUseCase {

  void like(UUID commentId, UUID accountId);

  void unlike(UUID commentId, UUID accountId);

  boolean isLiked(UUID commentId, UUID accountId);
}
