package dev.civicpulse.identity.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class AccountTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Test
  void registerCreatesUnverifiedAccountWithDocumentData() {
    Account account =
        Account.register(
            AccountId.generate(),
            AccountType.CITIZEN,
            "Jane Doe",
            "janedoe",
            "jane@example.com",
            "hashed-password",
            DocumentType.CPF,
            "hashed-cpf",
            new byte[] {1, 2, 3},
            NOW);

    assertThat(account.verified()).isFalse();
    assertThat(account.isAnonymized()).isFalse();
    assertThat(account.documentType()).contains(DocumentType.CPF);
    assertThat(account.documentNumberHash()).contains("hashed-cpf");
    assertThat(account.createdAt()).isEqualTo(NOW);
    assertThat(account.updatedAt()).isEqualTo(NOW);
  }

  @Test
  void adminAccountDoesNotRequireDocumentData() {
    Account admin =
        Account.register(
            AccountId.generate(),
            AccountType.ADMIN,
            "Platform Admin",
            "admin",
            "admin@example.com",
            "hashed-password",
            null,
            null,
            null,
            NOW);

    assertThat(admin.documentType()).isEmpty();
    assertThat(admin.documentNumberHash()).isEmpty();
  }

  @Test
  void nonAdminAccountRequiresDocumentNumberHash() {
    assertThatThrownBy(
            () ->
                Account.register(
                    AccountId.generate(),
                    AccountType.CITIZEN,
                    "Jane Doe",
                    "janedoe",
                    "jane@example.com",
                    "hashed-password",
                    DocumentType.CPF,
                    null,
                    null,
                    NOW))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("documentNumberHash");
  }

  @Test
  void blankNameIsRejected() {
    assertThatThrownBy(
            () ->
                Account.register(
                    AccountId.generate(),
                    AccountType.ADMIN,
                    "  ",
                    "admin",
                    "admin@example.com",
                    "hashed-password",
                    null,
                    null,
                    null,
                    NOW))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("name");
  }

  @Test
  void markVerifiedIsIdempotent() {
    Account account = registerCitizen();

    account.markVerified(NOW);
    assertThat(account.verified()).isTrue();
    assertThat(account.updatedAt()).isEqualTo(NOW);

    Instant later = NOW.plusSeconds(60);
    account.markVerified(later);
    assertThat(account.updatedAt()).isEqualTo(NOW); // second call is a no-op, timestamp unchanged
  }

  @Test
  void anonymizeOverwritesPiiInPlaceAndIsIdempotent() {
    Account account = registerCitizen();
    account.avatarUrl();

    Instant anonymizedAt = NOW.plusSeconds(120);
    account.anonymize(anonymizedAt);

    assertThat(account.isAnonymized()).isTrue();
    assertThat(account.name()).isEqualTo("Deleted account");
    assertThat(account.passwordHash()).isEmpty();
    assertThat(account.avatarUrl()).isEmpty();
    assertThat(account.anonymizedAt()).contains(anonymizedAt);

    // Second call must not overwrite the original anonymization timestamp.
    account.anonymize(anonymizedAt.plusSeconds(60));
    assertThat(account.anonymizedAt()).contains(anonymizedAt);
  }

  private static Account registerCitizen() {
    return Account.register(
        AccountId.generate(),
        AccountType.CITIZEN,
        "Jane Doe",
        "janedoe",
        "jane@example.com",
        "hashed-password",
        DocumentType.CPF,
        "hashed-cpf",
        new byte[] {1, 2, 3},
        NOW);
  }
}
