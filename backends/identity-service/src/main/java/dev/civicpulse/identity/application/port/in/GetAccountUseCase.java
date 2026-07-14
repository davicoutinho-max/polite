package dev.civicpulse.identity.application.port.in;

import dev.civicpulse.identity.domain.model.Account;
import dev.civicpulse.identity.domain.model.AccountId;
import java.util.Set;

public interface GetAccountUseCase {

  Account getById(AccountId id);

  Set<String> getPermissions(AccountId id);
}
