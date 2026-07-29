package dev.civicpulse.identity.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import dev.civicpulse.identity.application.port.out.AccountRepository;
import dev.civicpulse.identity.application.port.out.DocumentCipher;
import dev.civicpulse.identity.application.port.out.RoleRepository;
import dev.civicpulse.identity.domain.model.Account;
import dev.civicpulse.identity.domain.model.AccountId;
import dev.civicpulse.identity.domain.model.AccountType;
import dev.civicpulse.identity.domain.model.DocumentType;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetAccountServiceTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Mock private AccountRepository accountRepository;
  @Mock private RoleRepository roleRepository;
  @Mock private DocumentCipher documentCipher;

  @Test
  void getPaymentProfileDecryptsTheRealDocumentNumber() {
    var service = new GetAccountService(accountRepository, roleRepository, documentCipher);
    AccountId id = AccountId.generate();
    byte[] encrypted = new byte[] {1, 2, 3};
    Account account =
        Account.register(id, AccountType.CITIZEN, "Jane Doe", "janedoe", "jane@example.com", "hash", DocumentType.CPF, "hashed-cpf", encrypted, NOW);
    when(accountRepository.findById(id)).thenReturn(Optional.of(account));
    when(documentCipher.decrypt(encrypted)).thenReturn("52998224725");

    var profile = service.getPaymentProfile(id);

    assertThat(profile.name()).isEqualTo("Jane Doe");
    assertThat(profile.documentNumber()).isEqualTo("52998224725");
  }

  @Test
  void getPaymentProfileThrowsWhenAccountHasNoDocumentOnFile() {
    var service = new GetAccountService(accountRepository, roleRepository, documentCipher);
    AccountId id = AccountId.generate();
    Account admin = Account.register(id, AccountType.ADMIN, "Admin", "admin", "admin@example.com", "hash", null, null, null, NOW);
    when(accountRepository.findById(id)).thenReturn(Optional.of(admin));

    assertThatThrownBy(() -> service.getPaymentProfile(id)).isInstanceOf(IllegalArgumentException.class);
  }
}
