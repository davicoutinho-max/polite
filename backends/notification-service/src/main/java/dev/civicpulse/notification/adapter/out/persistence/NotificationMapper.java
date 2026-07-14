package dev.civicpulse.notification.adapter.out.persistence;

import dev.civicpulse.notification.domain.model.Notification;
import org.springframework.stereotype.Component;

@Component
class NotificationMapper {

  Notification toDomain(NotificationJpaEntity entity) {
    return Notification.reconstitute(
        entity.getId(),
        entity.getRecipientAccountId(),
        entity.getCategory(),
        entity.getIcon(),
        entity.getTitle(),
        entity.getMessage(),
        entity.getLink(),
        entity.getSourceEventId(),
        entity.isRead(),
        entity.getCreatedAt());
  }

  NotificationJpaEntity toEntity(Notification notification) {
    return new NotificationJpaEntity(
        notification.id(),
        notification.recipientAccountId(),
        notification.category(),
        notification.icon().orElse(null),
        notification.title(),
        notification.message(),
        notification.link().orElse(null),
        notification.sourceEventId(),
        notification.read(),
        notification.createdAt());
  }
}
