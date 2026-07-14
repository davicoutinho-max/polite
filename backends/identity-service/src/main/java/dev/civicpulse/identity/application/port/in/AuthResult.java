package dev.civicpulse.identity.application.port.in;

import java.time.Instant;
import java.util.UUID;

public record AuthResult(
    UUID accountId, String accessToken, Instant accessTokenExpiresAt, String refreshToken) {}
