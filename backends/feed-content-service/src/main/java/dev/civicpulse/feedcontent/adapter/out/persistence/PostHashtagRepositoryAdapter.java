package dev.civicpulse.feedcontent.adapter.out.persistence;

import dev.civicpulse.feedcontent.application.port.out.PostHashtagRepository;
import dev.civicpulse.feedcontent.domain.model.PostHashtag;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
class PostHashtagRepositoryAdapter implements PostHashtagRepository {

  private final PostHashtagJpaRepository jpaRepository;

  PostHashtagRepositoryAdapter(PostHashtagJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public PostHashtag save(PostHashtag postHashtag) {
    var saved =
        jpaRepository.save(
            new PostHashtagJpaEntity(postHashtag.id().orElse(null), postHashtag.postId(), postHashtag.hashtag(), postHashtag.createdAt()));
    return PostHashtag.reconstitute(saved.getId(), saved.getPostId(), saved.getHashtag(), saved.getCreatedAt());
  }

  @Override
  public List<HashtagCount> countByHashtagSince(Instant since, int limit) {
    return jpaRepository.countByHashtagSince(since, PageRequest.of(0, limit)).stream()
        .map(projection -> new HashtagCount(projection.getHashtag(), projection.getCount()))
        .toList();
  }
}
