package dev.civicpulse.identity.application.port.in;

import dev.civicpulse.identity.domain.model.AccountType;
import dev.civicpulse.identity.domain.model.RegistrationToken;
import java.util.List;
import java.util.UUID;

/** Issuing side of the invite-token flow — called by platform-configuration-service (party
 * invites) and party-management-service (politician invites) on behalf of whoever is inviting
 * someone. See RegistrationToken's javadoc for the full picture. */
public interface IssueRegistrationTokenUseCase {

  RegistrationToken issue(AccountType accountType, UUID issuedByAccountId, String targetEmail, String prefillDataJson);

  /** Invalidates the given token (if it belongs to {@code issuedByAccountId} and isn't already
   * consumed) and issues + emails a fresh replacement with the same account type/email/prefill
   * data. */
  RegistrationToken resend(UUID tokenId, UUID issuedByAccountId);

  List<RegistrationToken> listIssuedBy(UUID issuedByAccountId);
}
