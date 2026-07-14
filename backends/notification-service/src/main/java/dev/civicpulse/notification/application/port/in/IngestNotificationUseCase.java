package dev.civicpulse.notification.application.port.in;

import dev.civicpulse.notification.domain.model.NotificationCategory;
import java.util.UUID;

public interface IngestNotificationUseCase {

  /** Idempotent under Kafka redelivery — a repeat call with the same
   * ({@code recipientAccountId}, {@code sourceEventId}) pair is a no-op (see schema.sql's note on
   * {@code source_event_id}). */
  void ingest(UUID recipientAccountId, NotificationCategory category, String icon, String title, String message, String link, String sourceEventId);
}
