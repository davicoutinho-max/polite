package dev.civicpulse.notification.adapter.in.web.dto;

import dev.civicpulse.notification.domain.model.Notification;
import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
    UUID id, UUID recipientAccountId, String category, String icon, String title, String message, String link, boolean read, Instant createdAt) {

  public static NotificationResponse from(Notification notification) {
    return new NotificationResponse(
        notification.id(),
        notification.recipientAccountId(),
        notification.category().code(),
        notification.icon().orElse(null),
        notification.title(),
        notification.message(),
        notification.link().orElse(null),
        notification.read(),
        notification.createdAt());
  }
}
