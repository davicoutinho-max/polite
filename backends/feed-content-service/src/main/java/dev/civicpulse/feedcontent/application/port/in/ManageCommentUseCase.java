package dev.civicpulse.feedcontent.application.port.in;

import dev.civicpulse.feedcontent.domain.model.Comment;
import java.util.List;
import java.util.UUID;

public interface ManageCommentUseCase {

  /** {@code parentCommentId} is null for a top-level comment, or the id of the comment being
   * replied to — one level of threading only. */
  Comment addComment(UUID postId, UUID authorAccountId, UUID parentCommentId, String body);

  List<Comment> listByPost(UUID postId);
}
