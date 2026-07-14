package dev.civicpulse.notification.application.port.out;

import dev.civicpulse.notification.domain.model.Notification;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository {

  Notification save(Notification notification);

  Optional<Notification> findById(UUID id);

  List<Notification> findByRecipient(UUID recipientAccountId, int page, int pageSize);

  long countUnread(UUID recipientAccountId);

  boolean existsByRecipientAndSourceEventId(UUID recipientAccountId, String sourceEventId);
}
