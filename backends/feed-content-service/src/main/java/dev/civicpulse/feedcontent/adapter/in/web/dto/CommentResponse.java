package dev.civicpulse.feedcontent.adapter.in.web.dto;

import dev.civicpulse.feedcontent.domain.model.Comment;
import java.time.Instant;
import java.util.UUID;

public record CommentResponse(UUID id, UUID postId, UUID authorAccountId, String body, Instant createdAt) {

  public static CommentResponse from(Comment comment) {
    return new CommentResponse(comment.id(), comment.postId(), comment.authorAccountId(), comment.body(), comment.createdAt());
  }
}
