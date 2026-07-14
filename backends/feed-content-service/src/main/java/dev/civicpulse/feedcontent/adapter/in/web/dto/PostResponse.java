package dev.civicpulse.feedcontent.adapter.in.web.dto;

import dev.civicpulse.feedcontent.domain.model.Post;
import java.time.Instant;
import java.util.UUID;

public record PostResponse(
    UUID id,
    UUID authorAccountId,
    String kind,
    String content,
    String imageUrl,
    String visibility,
    String context,
    UUID liveSessionId,
    Instant createdAt) {

  public static PostResponse from(Post post) {
    return new PostResponse(
        post.id(),
        post.authorAccountId(),
        post.kind().code(),
        post.content().orElse(null),
        post.imageUrl().orElse(null),
        post.visibility().code(),
        post.context().orElse(null),
        post.liveSessionId().orElse(null),
        post.createdAt());
  }
}
