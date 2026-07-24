package dev.civicpulse.feedcontent.adapter.out.persistence;

import dev.civicpulse.feedcontent.domain.model.Post;
import org.springframework.stereotype.Component;

@Component
class PostMapper {

  Post toDomain(PostJpaEntity entity) {
    return Post.reconstitute(
        entity.getId(),
        entity.getAuthorAccountId(),
        entity.getKind(),
        entity.getContent(),
        entity.getImageUrl(),
        entity.getFileUrl(),
        entity.getFileName(),
        entity.getVisibility(),
        entity.getContext(),
        entity.getLiveSessionId(),
        entity.getCreatedAt(),
        entity.getPollClosesAt());
  }

  PostJpaEntity toEntity(Post post) {
    return new PostJpaEntity(
        post.id(),
        post.authorAccountId(),
        post.kind(),
        post.content().orElse(null),
        post.imageUrl().orElse(null),
        post.fileUrl().orElse(null),
        post.fileName().orElse(null),
        post.visibility(),
        post.context().orElse(null),
        post.liveSessionId().orElse(null),
        post.createdAt(),
        post.pollClosesAt().orElse(null));
  }
}
