package dev.civicpulse.feedcontent.application.port.out;

import dev.civicpulse.feedcontent.domain.model.PostHashtag;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface PostHashtagRepository {

  PostHashtag save(PostHashtag postHashtag);

  List<HashtagCount> countByHashtagSince(Instant since, int limit);

  void deleteByPostId(UUID postId);

  record HashtagCount(String hashtag, long count) {}
}
