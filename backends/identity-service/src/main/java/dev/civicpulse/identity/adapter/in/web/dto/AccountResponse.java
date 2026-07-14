package dev.civicpulse.identity.adapter.in.web.dto;

import dev.civicpulse.identity.domain.model.Account;
import java.time.Instant;
import java.util.UUID;

public record AccountResponse(
    UUID id,
    String accountType,
    String name,
    String handle,
    String email,
    boolean verified,
    String avatarUrl,
    Instant createdAt) {

  public static AccountResponse from(Account account) {
    return new AccountResponse(
        account.id().value(),
        account.accountType().code(),
        account.name(),
        account.handle(),
        account.email(),
        account.verified(),
        account.avatarUrl().orElse(null),
        account.createdAt());
  }
}
