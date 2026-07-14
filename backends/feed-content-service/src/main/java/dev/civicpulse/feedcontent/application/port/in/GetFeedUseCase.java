package dev.civicpulse.feedcontent.application.port.in;

import dev.civicpulse.feedcontent.domain.model.Post;
import dev.civicpulse.feedcontent.domain.model.PostMetrics;
import java.util.List;
import java.util.UUID;

public interface GetFeedUseCase {

  Post getById(UUID id);

  PostMetrics getMetrics(UUID postId);

  List<Post> getByAuthor(UUID authorAccountId, int page, int pageSize);

  List<Post> getPublicFeed(int page, int pageSize);
}
