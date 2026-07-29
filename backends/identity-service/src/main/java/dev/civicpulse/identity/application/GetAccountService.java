package dev.civicpulse.identity.application;

import dev.civicpulse.identity.application.port.in.GetAccountUseCase;
import dev.civicpulse.identity.application.port.out.AccountRepository;
import dev.civicpulse.identity.application.port.out.DocumentCipher;
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
  private final DocumentCipher documentCipher;

  public GetAccountService(AccountRepository accountRepository, RoleRepository roleRepository, DocumentCipher documentCipher) {
    this.accountRepository = accountRepository;
    this.roleRepository = roleRepository;
    this.documentCipher = documentCipher;
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

  @Override
  @Transactional(readOnly = true)
  public PaymentProfile getPaymentProfile(AccountId id) {
    Account account = getById(id);
    byte[] encrypted =
        account.documentNumberEncrypted().orElseThrow(() -> new IllegalArgumentException("Account " + id + " has no document number on file"));
    return new PaymentProfile(account.name(), documentCipher.decrypt(encrypted));
  }
}
