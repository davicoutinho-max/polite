package dev.civicpulse.feedcontent.application.port.out;

import dev.civicpulse.feedcontent.domain.model.Comment;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommentRepository {

  Comment save(Comment comment);

  Optional<Comment> findById(UUID commentId);

  List<Comment> findByPostId(UUID postId);

  void deleteByPostId(UUID postId);
}
