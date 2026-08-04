package dev.civicpulse.feedcontent.application.port.out;

import dev.civicpulse.feedcontent.domain.model.CommentLike;
import java.util.UUID;

public interface CommentLikeRepository {

  CommentLike save(CommentLike like);

  void delete(UUID commentId, UUID accountId);

  boolean exists(UUID commentId, UUID accountId);
}
