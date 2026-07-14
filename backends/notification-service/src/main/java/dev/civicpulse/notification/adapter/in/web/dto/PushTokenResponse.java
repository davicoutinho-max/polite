package dev.civicpulse.notification.adapter.in.web.dto;

import dev.civicpulse.notification.domain.model.PushToken;
import java.time.Instant;
import java.util.UUID;

public record PushTokenResponse(UUID accountId, String platform, String token, Instant updatedAt) {

  public static PushTokenResponse from(PushToken pushToken) {
    return new PushTokenResponse(pushToken.accountId(), pushToken.platform().code(), pushToken.token(), pushToken.updatedAt());
  }
}
