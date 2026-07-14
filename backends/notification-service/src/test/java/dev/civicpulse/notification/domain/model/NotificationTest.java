package dev.civicpulse.notification.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class NotificationTest {

  @Test
  void createStartsUnread() {
    Notification notification =
        Notification.create(
            UUID.randomUUID(), UUID.randomUUID(), NotificationCategory.PARTY, "icon", "title", "message", "/link", "src-1", Instant.now());

    assertThat(notification.read()).isFalse();
  }

  @Test
  void createRejectsBlankTitle() {
    assertThatThrownBy(
            () ->
                Notification.create(
                    UUID.randomUUID(), UUID.randomUUID(), NotificationCategory.PARTY, null, " ", "message", null, "src-1", Instant.now()))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void createRejectsBlankSourceEventId() {
    assertThatThrownBy(
            () ->
                Notification.create(
                    UUID.randomUUID(), UUID.randomUUID(), NotificationCategory.PARTY, null, "title", "message", null, " ", Instant.now()))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void markReadSetsReadTrue() {
    Notification notification =
        Notification.create(
            UUID.randomUUID(), UUID.randomUUID(), NotificationCategory.CAMPAIGN, null, "title", "message", null, "src-1", Instant.now());

    notification.markRead();

    assertThat(notification.read()).isTrue();
  }
}
