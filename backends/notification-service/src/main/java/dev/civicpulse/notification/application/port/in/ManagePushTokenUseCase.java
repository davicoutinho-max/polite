package dev.civicpulse.notification.application.port.in;

import dev.civicpulse.notification.domain.model.NotificationPlatform;
import dev.civicpulse.notification.domain.model.PushToken;
import java.util.List;
import java.util.UUID;

public interface ManagePushTokenUseCase {

  PushToken register(UUID accountId, NotificationPlatform platform, String token);

  List<PushToken> listByAccount(UUID accountId);

  void unregister(UUID accountId, NotificationPlatform platform, String token);
}
