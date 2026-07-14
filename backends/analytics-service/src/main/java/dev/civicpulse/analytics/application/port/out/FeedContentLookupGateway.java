package dev.civicpulse.analytics.application.port.out;

import java.util.Optional;
import java.util.UUID;

/** {@code PostLiked}/{@code CommentAdded} don't carry the post's author or content kind (and
 * {@code CommentAdded} doesn't even carry the commenter) — this gateway resolves both from
 * feed-content-service. */
public interface FeedContentLookupGateway {

  Optional<PostSummary> lookupPost(UUID postId);

  Optional<UUID> lookupCommentAuthor(UUID postId, UUID commentId);

  record PostSummary(UUID authorAccountId, String kind) {}
}
