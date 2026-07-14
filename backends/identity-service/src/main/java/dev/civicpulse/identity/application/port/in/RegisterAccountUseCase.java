package dev.civicpulse.identity.application.port.in;

import dev.civicpulse.identity.domain.model.Account;
import dev.civicpulse.identity.domain.model.AccountType;
import dev.civicpulse.identity.domain.model.DocumentType;

public interface RegisterAccountUseCase {

  /** Public self-registration — the web adapter forces {@code accountType = CITIZEN} here;
   * see docs/architecture (politician/party/admin accounts are never self-service). */
  Account registerCitizen(RegisterAccountCommand command);

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
