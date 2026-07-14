package dev.civicpulse.identity.application.port.in;

import dev.civicpulse.identity.domain.model.AccountId;

public interface ManageSessionUseCase {

  AuthResult refresh(String rawRefreshToken, String userAgent, String ipAddress);

  void revoke(String rawRefreshToken);

  /** "Logout everywhere" — revokes every active session for the account. */
  void revokeAll(AccountId accountId);
}
