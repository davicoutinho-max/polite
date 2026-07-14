package dev.civicpulse.notification.application.port.in;

import dev.civicpulse.notification.domain.model.Notification;
import java.util.List;
import java.util.UUID;

public interface GetNotificationUseCase {

  List<Notification> listByRecipient(UUID recipientAccountId, int page, int pageSize);

  long countUnread(UUID recipientAccountId);

  void markRead(UUID notificationId);
}
