package dev.civicpulse.notification.application.port.out;

import dev.civicpulse.notification.domain.model.PushToken;
import java.util.List;
import java.util.UUID;

public interface PushTokenRepository {

  PushToken save(PushToken pushToken);

  List<PushToken> findByAccountId(UUID accountId);

  void delete(UUID accountId, String platform, String token);
}
