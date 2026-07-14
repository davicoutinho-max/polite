package dev.civicpulse.feedcontent.application.port.out;

import dev.civicpulse.feedcontent.domain.model.Like;
import java.util.UUID;

public interface LikeRepository {

  Like save(Like like);

  void delete(UUID postId, UUID accountId);

  boolean exists(UUID postId, UUID accountId);
}
