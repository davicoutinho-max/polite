package dev.civicpulse.notification.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PushTokenTest {

  @Test
  void registerRejectsBlankToken() {
    assertThatThrownBy(() -> PushToken.register(UUID.randomUUID(), NotificationPlatform.ANDROID, " ", Instant.now()))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void refreshUpdatesTimestamp() {
    Instant t0 = Instant.parse("2026-01-01T00:00:00Z");
    Instant t1 = Instant.parse("2026-01-01T01:00:00Z");
    PushToken token = PushToken.register(UUID.randomUUID(), NotificationPlatform.IOS, "tok-1", t0);

    token.refresh(t1);

    assertThat(token.updatedAt()).isEqualTo(t1);
  }
}
