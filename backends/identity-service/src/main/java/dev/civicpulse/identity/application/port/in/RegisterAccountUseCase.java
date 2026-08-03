package dev.civicpulse.identity.application.port.in;

import dev.civicpulse.identity.domain.model.Account;
import dev.civicpulse.identity.domain.model.AccountId;
import dev.civicpulse.identity.domain.model.AccountType;
import dev.civicpulse.identity.domain.model.DocumentType;

public interface RegisterAccountUseCase {

  /** Public self-registration — the web adapter forces {@code accountType = CITIZEN} here;
   * see docs/architecture (politician/party/admin accounts are never self-service). Ends up a
   * plain citizen unless the CPF/CNPJ happens to exact-match an unclaimed synced profile (see
   * {@code RegisterAccountService}'s existing claim-by-hash behavior), in which case the real
   * account type/identity of that profile wins instead. */
  Account registerCitizen(RegisterAccountCommand command);

  /** Same as {@link #registerCitizen(RegisterAccountCommand)}, but the citizen has already
   * confirmed (via {@link CheckDocumentUseCase} or the directory-search fallback) that a
   * specific unclaimed synced profile is really them — claims that exact account instead of
   * relying on an exact document-hash match, which never fires for state/municipal politicians
   * or parties (synthetic document numbers — see DocumentNumberFallback). */
  Account registerCitizen(RegisterAccountCommand command, AccountId claimAccountId);

  /** Internal provisioning path — called by Party Management when a party registers a
   * politician, or by a platform-admin flow. Not exposed to the public internet by the
   * Gateway's routing table. */
  Account provisionAccount(AccountType accountType, RegisterAccountCommand command);

  record RegisterAccountCommand(
      String name,
      String handle,
      String email,
      String rawPassword,
      DocumentType documentType,
      String rawDocumentNumber) {}
}
