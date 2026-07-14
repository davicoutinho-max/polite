package dev.civicpulse.feedcontent.application.port.out;

import dev.civicpulse.feedcontent.domain.model.PostHashtag;
import java.time.Instant;
import java.util.List;

public interface PostHashtagRepository {

  PostHashtag save(PostHashtag postHashtag);

  List<HashtagCount> countByHashtagSince(Instant since, int limit);

  record HashtagCount(String hashtag, long count) {}
}
