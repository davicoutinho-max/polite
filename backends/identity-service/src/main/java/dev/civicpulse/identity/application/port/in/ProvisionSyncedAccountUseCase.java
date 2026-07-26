package dev.civicpulse.identity.application.port.in;

import dev.civicpulse.identity.domain.model.Account;
import dev.civicpulse.identity.domain.model.AccountType;
import dev.civicpulse.identity.domain.model.DocumentType;

/** Provisioning path for government-data-sync jobs (see government-sync-service) — accounts
 * created here belong to a real deputy/senator/party who never signed up, so there is no
 * password to capture. Idempotent by {@code (externalSource, externalId)}: re-running the sync
 * job updates the existing account's display fields instead of creating a duplicate. */
public interface ProvisionSyncedAccountUseCase {

  Account provisionOrUpdate(AccountType accountType, ProvisionSyncedAccountCommand command);

  record ProvisionSyncedAccountCommand(
      String name,
      String handle,
      String email,
      String avatarUrl,
      DocumentType documentType,
      String rawDocumentNumber,
      String externalSource,
      String externalId) {}
}
