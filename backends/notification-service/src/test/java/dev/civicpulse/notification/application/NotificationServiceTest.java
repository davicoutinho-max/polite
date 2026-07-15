package dev.civicpulse.notification.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.civicpulse.notification.application.port.out.NotificationRepository;
import dev.civicpulse.notification.domain.exception.NotificationNotFoundException;
import dev.civicpulse.notification.domain.model.Notification;
import dev.civicpulse.notification.domain.model.NotificationCategory;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Mock private NotificationRepository notificationRepository;

  private NotificationService service;

  @BeforeEach
  void setUp() {
    service = new NotificationService(notificationRepository, Clock.fixed(NOW, ZoneOffset.UTC));
  }

  @Test
  void ingestSkipsWhenAlreadyProcessed() {
    UUID recipientId = UUID.randomUUID();
    when(notificationRepository.existsByRecipientAndSourceEventId(recipientId, "src-1")).thenReturn(true);

    service.ingest(recipientId, NotificationCategory.PARTY, null, "title", "message", null, "src-1");

    verify(notificationRepository, never()).save(any());
  }

  @Test
  void ingestSavesNewNotification() {
    UUID recipientId = UUID.randomUUID();
    when(notificationRepository.existsByRecipientAndSourceEventId(recipientId, "src-1")).thenReturn(false);

    service.ingest(recipientId, NotificationCategory.CAMPAIGN, "icon", "title", "message", "/link", "src-1");

    ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
    verify(notificationRepository).save(captor.capture());
    assertThat(captor.getValue().recipientAccountId()).isEqualTo(recipientId);
    assertThat(captor.getValue().sourceEventId()).isEqualTo("src-1");
  }

  @Test
  void markReadThrowsWhenMissing() {
    UUID id = UUID.randomUUID();
    when(notificationRepository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.markRead(id)).isInstanceOf(NotificationNotFoundException.class);
  }

  @Test
  void markReadUpdatesNotification() {
    UUID id = UUID.randomUUID();
    Notification notification =
        Notification.create(id, UUID.randomUUID(), NotificationCategory.PARTY, null, "title", "message", null, "src-1", NOW);
    when(notificationRepository.findById(id)).thenReturn(Optional.of(notification));

    service.markRead(id);

    assertThat(notification.read()).isTrue();
    verify(notificationRepository).save(notification);
  }

  @Test
  void markAllReadOnlyUpdatesUnreadOnes() {
    UUID recipientId = UUID.randomUUID();
    Notification unread =
        Notification.create(UUID.randomUUID(), recipientId, NotificationCategory.PARTY, null, "title", "message", null, "src-1", NOW);
    Notification alreadyRead =
        Notification.create(UUID.randomUUID(), recipientId, NotificationCategory.PARTY, null, "title2", "message2", null, "src-2", NOW);
    alreadyRead.markRead();
    when(notificationRepository.findByRecipient(recipientId, 0, 1000)).thenReturn(List.of(unread, alreadyRead));

    service.markAllRead(recipientId);

    assertThat(unread.read()).isTrue();
    verify(notificationRepository).save(unread);
    verify(notificationRepository, never()).save(alreadyRead);
  }
}
