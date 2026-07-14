package dev.civicpulse.notification.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.civicpulse.notification.application.port.out.PushTokenRepository;
import dev.civicpulse.notification.domain.model.NotificationPlatform;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PushTokenServiceTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Mock private PushTokenRepository pushTokenRepository;

  private PushTokenService service;

  @BeforeEach
  void setUp() {
    service = new PushTokenService(pushTokenRepository, Clock.fixed(NOW, ZoneOffset.UTC));
  }

  @Test
  void registerSavesToken() {
    when(pushTokenRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    UUID accountId = UUID.randomUUID();

    var token = service.register(accountId, NotificationPlatform.ANDROID, "tok-1");

    assertThat(token.accountId()).isEqualTo(accountId);
    assertThat(token.token()).isEqualTo("tok-1");
  }

  @Test
  void unregisterDelegatesToRepository() {
    UUID accountId = UUID.randomUUID();

    service.unregister(accountId, NotificationPlatform.WEB, "tok-1");

    verify(pushTokenRepository).delete(accountId, "web", "tok-1");
  }
}
