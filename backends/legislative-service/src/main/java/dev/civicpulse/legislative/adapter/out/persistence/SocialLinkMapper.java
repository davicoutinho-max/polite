package dev.civicpulse.legislative.adapter.out.persistence;

import dev.civicpulse.legislative.domain.model.SocialLink;
import org.springframework.stereotype.Component;

@Component
class SocialLinkMapper {

  SocialLink toDomain(SocialLinkJpaEntity entity) {
    return SocialLink.reconstitute(
        entity.getId(), entity.getPoliticianAccountId(), entity.getPlatform(), entity.getLabel(), entity.getHandle(), entity.getUrl());
  }

  SocialLinkJpaEntity toEntity(SocialLink socialLink) {
    return new SocialLinkJpaEntity(
        socialLink.id().orElse(null),
        socialLink.politicianAccountId(),
        socialLink.platform(),
        socialLink.label(),
        socialLink.handle(),
        socialLink.url());
  }
}
