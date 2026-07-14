package dev.civicpulse.feedcontent.application.port.in;

import dev.civicpulse.feedcontent.domain.model.Comment;
import java.util.List;
import java.util.UUID;

public interface ManageCommentUseCase {

  Comment addComment(UUID postId, UUID authorAccountId, String body);

  List<Comment> listByPost(UUID postId);
}
