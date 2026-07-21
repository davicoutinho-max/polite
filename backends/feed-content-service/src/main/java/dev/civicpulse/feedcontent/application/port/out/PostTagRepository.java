package dev.civicpulse.feedcontent.application.port.out;

import dev.civicpulse.feedcontent.domain.model.PostTag;
import java.util.List;
import java.util.UUID;

public interface PostTagRepository {

  PostTag save(PostTag tag);

  List<PostTag> findByPostId(UUID postId);

  void deleteByPostId(UUID postId);
}
