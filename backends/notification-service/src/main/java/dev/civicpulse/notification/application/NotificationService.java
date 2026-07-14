package dev.civicpulse.notification.application;

import dev.civicpulse.notification.application.port.in.GetNotificationUseCase;
import dev.civicpulse.notification.application.port.in.IngestNotificationUseCase;
import dev.civicpulse.notification.application.port.out.NotificationRepository;
import dev.civicpulse.notification.domain.exception.NotificationNotFoundException;
import dev.civicpulse.notification.domain.model.Notification;
import dev.civicpulse.notification.domain.model.NotificationCategory;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService implements IngestNotificationUseCase, GetNotificationUseCase {

  private final NotificationRepository notificationRepository;
  private final Clock clock;

  public NotificationService(NotificationRepository notificationRepository, Clock clock) {
    this.notificationRepository = notificationRepository;
    this.clock = clock;
  }

  @Override
  @Transactional
  public void ingest(
      UUID recipientAccountId, NotificationCategory category, String icon, String title, String message, String link, String sourceEventId) {
    if (notificationRepository.existsByRecipientAndSourceEventId(recipientAccountId, sourceEventId)) {
      return; // idempotent — reprocessed message
    }
    notificationRepository.save(
        Notification.create(UUID.randomUUID(), recipientAccountId, category, icon, title, message, link, sourceEventId, clock.instant()));
  }

  @Override
  @Transactional(readOnly = true)
  public List<Notification> listByRecipient(UUID recipientAccountId, int page, int pageSize) {
    return notificationRepository.findByRecipient(recipientAccountId, page, pageSize);
  }

  @Override
  @Transactional(readOnly = true)
  public long countUnread(UUID recipientAccountId) {
    return notificationRepository.countUnread(recipientAccountId);
  }

  @Override
  @Transactional
  public void markRead(UUID notificationId) {
    Notification notification = notificationRepository.findById(notificationId).orElseThrow(() -> new NotificationNotFoundException(notificationId));
    notification.markRead();
    notificationRepository.save(notification);
  }
}
