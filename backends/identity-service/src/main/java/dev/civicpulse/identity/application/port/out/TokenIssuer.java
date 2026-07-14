package dev.civicpulse.identity.application.port.out;

import dev.civicpulse.identity.domain.model.AccountId;
import dev.civicpulse.identity.domain.model.AccountType;
import java.time.Instant;
import java.util.Set;

/** JWT issuance/parsing adapter. The API Gateway validates the signature and expiry on every
 * request (see system-architecture.html's "JWT validated once, at the edge") — this port is
 * what actually mints and can locally verify those tokens. */
public interface TokenIssuer {

  IssuedToken issueAccessToken(AccountId accountId, AccountType accountType, Set<String> permissions, Instant now);

  String generateRefreshToken();

  String hashRefreshToken(String rawRefreshToken);

  record IssuedToken(String token, Instant expiresAt) {}
}
