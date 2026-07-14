package dev.civicpulse.feedcontent.application.port.out;

import dev.civicpulse.feedcontent.domain.model.Post;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PostRepository {

  Post save(Post post);

  Optional<Post> findById(UUID id);

  List<Post> findByAuthorAccountId(UUID authorAccountId, int page, int pageSize);

  /** Reverse-chronological, public posts only — the simplest "Latest" feed sort (see
   * FeedContentServiceApplication's note on the ranked/Following feed not being implemented in
   * this pass). */
  List<Post> findPublicFeed(int page, int pageSize);
}
