package dev.civicpulse.feedcontent.adapter.out.persistence;

import dev.civicpulse.feedcontent.application.port.out.PostTagRepository;
import dev.civicpulse.feedcontent.domain.model.PostTag;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class PostTagRepositoryAdapter implements PostTagRepository {

  private final PostTagJpaRepository jpaRepository;

  PostTagRepositoryAdapter(PostTagJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public PostTag save(PostTag tag) {
    var saved =
        jpaRepository.save(new PostTagJpaEntity(tag.id().orElse(null), tag.postId(), tag.label(), tag.severity().orElse(null), tag.icon().orElse(null)));
    return toDomain(saved);
  }

  @Override
  public List<PostTag> findByPostId(UUID postId) {
    return jpaRepository.findByPostId(postId).stream().map(PostTagRepositoryAdapter::toDomain).toList();
  }

  @Override
  public void deleteByPostId(UUID postId) {
    jpaRepository.deleteByPostId(postId);
  }

  private static PostTag toDomain(PostTagJpaEntity entity) {
    return PostTag.reconstitute(entity.getId(), entity.getPostId(), entity.getLabel(), entity.getSeverity(), entity.getIcon());
  }
}
