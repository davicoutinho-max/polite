package dev.civicpulse.identity.application;

import dev.civicpulse.identity.application.port.in.GetAccountUseCase;
import dev.civicpulse.identity.application.port.out.AccountRepository;
import dev.civicpulse.identity.application.port.out.RoleRepository;
import dev.civicpulse.identity.domain.exception.AccountNotFoundException;
import dev.civicpulse.identity.domain.model.Account;
import dev.civicpulse.identity.domain.model.AccountId;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetAccountService implements GetAccountUseCase {

  private final AccountRepository accountRepository;
  private final RoleRepository roleRepository;

  public GetAccountService(AccountRepository accountRepository, RoleRepository roleRepository) {
    this.accountRepository = accountRepository;
    this.roleRepository = roleRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public Account getById(AccountId id) {
    return accountRepository.findById(id).orElseThrow(() -> new AccountNotFoundException(id.toString()));
  }

  @Override
  @Transactional(readOnly = true)
  public Set<String> getPermissions(AccountId id) {
    Account account = getById(id);
    return roleRepository.findPermissionsByAccountType(account.accountType());
  }
}
