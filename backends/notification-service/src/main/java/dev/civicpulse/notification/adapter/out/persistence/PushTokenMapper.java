package dev.civicpulse.notification.adapter.out.persistence;

import dev.civicpulse.notification.domain.model.NotificationPlatform;
import dev.civicpulse.notification.domain.model.PushToken;
import org.springframework.stereotype.Component;

@Component
class PushTokenMapper {

  PushToken toDomain(PushTokenJpaEntity entity) {
    return PushToken.reconstitute(entity.getAccountId(), NotificationPlatform.fromCode(entity.getPlatform()), entity.getToken(), entity.getUpdatedAt());
  }

  PushTokenJpaEntity toEntity(PushToken pushToken) {
    return new PushTokenJpaEntity(pushToken.accountId(), pushToken.platform().code(), pushToken.token(), pushToken.updatedAt());
  }
}
