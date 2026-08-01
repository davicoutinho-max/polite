package dev.civicpulse.feedcontent.adapter.out.persistence;

import dev.civicpulse.feedcontent.domain.model.SocialShare;
import org.springframework.stereotype.Component;

@Component
class SocialShareMapper {

  SocialShare toDomain(SocialShareJpaEntity entity) {
    return SocialShare.reconstitute(
        entity.getId(),
        entity.getPostId(),
        entity.getPlatform(),
        entity.getStatus(),
        entity.getExternalPostId(),
        entity.getErrorMessage(),
        entity.getSharedAt());
  }

  SocialShareJpaEntity toEntity(SocialShare share) {
    return new SocialShareJpaEntity(
        share.id(),
        share.postId(),
        share.platform(),
        share.status(),
        share.externalPostId().orElse(null),
        share.errorMessage().orElse(null),
        share.sharedAt());
  }
}
