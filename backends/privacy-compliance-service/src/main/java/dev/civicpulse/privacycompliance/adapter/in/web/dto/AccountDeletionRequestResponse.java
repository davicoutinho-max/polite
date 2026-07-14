package dev.civicpulse.privacycompliance.adapter.in.web.dto;

import dev.civicpulse.privacycompliance.domain.model.AccountDeletionRequest;
import java.time.Instant;
import java.util.UUID;

public record AccountDeletionRequestResponse(UUID id, UUID accountId, String status, Instant requestedAt, Instant completedAt) {

  public static AccountDeletionRequestResponse from(AccountDeletionRequest request) {
    return new AccountDeletionRequestResponse(
        request.id(), request.accountId(), request.status().code(), request.requestedAt(), request.completedAt().orElse(null));
  }
}
