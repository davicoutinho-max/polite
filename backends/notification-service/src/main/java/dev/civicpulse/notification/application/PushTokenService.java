package dev.civicpulse.notification.application;

import dev.civicpulse.notification.application.port.in.ManagePushTokenUseCase;
import dev.civicpulse.notification.application.port.out.PushTokenRepository;
import dev.civicpulse.notification.domain.model.NotificationPlatform;
import dev.civicpulse.notification.domain.model.PushToken;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PushTokenService implements ManagePushTokenUseCase {

  private final PushTokenRepository pushTokenRepository;
  private final Clock clock;

  public PushTokenService(PushTokenRepository pushTokenRepository, Clock clock) {
    this.pushTokenRepository = pushTokenRepository;
    this.clock = clock;
  }

  @Override
  @Transactional
  public PushToken register(UUID accountId, NotificationPlatform platform, String token) {
    return pushTokenRepository.save(PushToken.register(accountId, platform, token, clock.instant()));
  }

  @Override
  @Transactional(readOnly = true)
  public List<PushToken> listByAccount(UUID accountId) {
    return pushTokenRepository.findByAccountId(accountId);
  }

  @Override
  @Transactional
  public void unregister(UUID accountId, NotificationPlatform platform, String token) {
    pushTokenRepository.delete(accountId, platform.code(), token);
  }
}
