package dev.civicpulse.identity.adapter.in.web.dto;

import dev.civicpulse.identity.application.port.in.AuthResult;
import java.time.Instant;
import java.util.UUID;

public record AuthResponse(UUID accountId, String accessToken, Instant accessTokenExpiresAt, String refreshToken) {

  public static AuthResponse from(AuthResult result) {
    return new AuthResponse(result.accountId(), result.accessToken(), result.accessTokenExpiresAt(), result.refreshToken());
  }
}
