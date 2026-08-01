package dev.civicpulse.feedcontent.adapter.out.persistence;

import dev.civicpulse.feedcontent.domain.model.SocialConnection;
import org.springframework.stereotype.Component;

@Component
class SocialConnectionMapper {

  SocialConnection toDomain(SocialConnectionJpaEntity entity) {
    return SocialConnection.reconstitute(
        entity.getId(),
        entity.getAccountId(),
        entity.getPlatform(),
        entity.getAccessToken(),
        entity.getExternalAccountId(),
        entity.getExternalAccountName(),
        entity.getConnectedAt());
  }

  SocialConnectionJpaEntity toEntity(SocialConnection connection) {
    return new SocialConnectionJpaEntity(
        connection.id(),
        connection.accountId(),
        connection.platform(),
        connection.accessToken(),
        connection.externalAccountId(),
        connection.externalAccountName().orElse(null),
        connection.connectedAt());
  }
}
