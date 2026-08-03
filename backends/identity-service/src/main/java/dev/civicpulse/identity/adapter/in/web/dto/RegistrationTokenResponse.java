package dev.civicpulse.identity.adapter.in.web.dto;

import dev.civicpulse.identity.domain.model.RegistrationToken;
import java.time.Instant;
import java.util.UUID;

public record RegistrationTokenResponse(
    UUID id, String token, String accountType, String targetEmail, String prefillData, String status, Instant createdAt, Instant expiresAt) {

  public static RegistrationTokenResponse from(RegistrationToken t, Instant now) {
    return new RegistrationTokenResponse(
        t.id(),
        t.token(),
        t.accountType().code(),
        t.targetEmail().orElse(null),
        t.prefillDataJson().orElse(null),
        t.status(now),
        t.createdAt(),
        t.expiresAt());
  }
}
