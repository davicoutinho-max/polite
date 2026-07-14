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

  boolean existsByEmail(String email);

  boolean existsByHandle(String handle);

  boolean existsByDocumentNumberHash(String documentNumberHash);
}
