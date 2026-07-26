package dev.civicpulse.identity.application.port.out;

import dev.civicpulse.identity.domain.model.Account;
import dev.civicpulse.identity.domain.model.AccountId;
import java.util.Optional;

/** Outbound port — implemented by the JPA adapter. The application layer only ever depends on
 * this interface, never on Spring Data directly. */
public interface AccountRepository {

  Account save(Account account);

  Optional<Account> findById(AccountId id);

  Optional<Account> findByEmail(String email);

  Optional<Account> findByHandle(String handle);

  /** The sync job's idempotency lookup — see {@link Account#registerSynced}. */
  Optional<Account> findByExternalSourceAndExternalId(String externalSource, String externalId);

  boolean existsByEmail(String email);

  boolean existsByHandle(String handle);

  boolean existsByDocumentNumberHash(String documentNumberHash);

  /** The claim-flow lookup — see {@link Account#claim}: a normal registration whose CPF/CNPJ
   * hash matches an existing (synced) account attaches credentials to it instead of creating a
   * duplicate. */
  Optional<Account> findByDocumentNumberHash(String documentNumberHash);
}
