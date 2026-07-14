package dev.civicpulse.identity.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import dev.civicpulse.identity.application.port.out.AccountRepository;
import dev.civicpulse.identity.domain.model.Account;
import dev.civicpulse.identity.domain.model.AccountId;
import dev.civicpulse.identity.domain.model.AccountType;
import dev.civicpulse.identity.domain.model.DocumentType;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Instant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Exercises the real JPA/Postgres adapter against the shared local-dev database provisioned by
 * {@code backends/docker-compose.yml} (identity_service / identity_service_app — see
 * backends/README.md). This intentionally does not use Testcontainers: on this machine, Docker
 * Desktop's named-pipe API is proxied in a way docker-java can't negotiate (fails before ever
 * reaching a real engine), while the docker CLI itself works fine — spinning up an ephemeral
 * container isn't worth trading for an unauthenticated TCP daemon socket just to route around
 * it. Pointing straight at the already-running compose Postgres exercises the exact same
 * Flyway migration + JPA mapping with a real Postgres engine.
 *
 * <p>Requires {@code docker compose up -d postgres} (see backends/README.md); skips itself with a
 * clear message if that database isn't reachable, e.g. in a CI runner without Docker.
 */
@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:postgresql://localhost:5432/identity_service",
      "spring.datasource.username=identity_service_app",
      "spring.datasource.password=identity_dev_pw"
    })
class AccountRepositoryIntegrationTest {

  @BeforeAll
  static void requireLocalPostgres() {
    boolean reachable;
    try (Socket socket = new Socket()) {
      socket.connect(new InetSocketAddress("localhost", 5432), 500);
      reachable = true;
    } catch (Exception e) {
      reachable = false;
    }
    assumeTrue(reachable, "Shared dev Postgres (localhost:5432) is not running — start it with "
        + "'docker compose up -d postgres' in backends/ to run this test");
  }

  @Autowired private AccountRepository accountRepository;

  @Test
  void savesAndRetrievesAccountByIdEmailAndHandle() {
    Instant now = Instant.now();
    Account account =
        Account.register(
            AccountId.generate(),
            AccountType.CITIZEN,
            "Jane Doe",
            "janedoe-" + System.nanoTime(),
            "jane-" + System.nanoTime() + "@example.com",
            "hashed-password",
            DocumentType.CPF,
            "hash-" + System.nanoTime(),
            new byte[] {1, 2, 3, 4},
            now);

    Account saved = accountRepository.save(account);

    assertThat(accountRepository.findById(saved.id())).isPresent().get().satisfies(found -> {
      assertThat(found.email()).isEqualTo(saved.email());
      assertThat(found.handle()).isEqualTo(saved.handle());
      assertThat(found.documentType()).contains(DocumentType.CPF);
      assertThat(found.documentNumberEncrypted()).isPresent();
    });

    assertThat(accountRepository.existsByEmail(saved.email())).isTrue();
    assertThat(accountRepository.existsByHandle(saved.handle())).isTrue();
    assertThat(accountRepository.existsByDocumentNumberHash(saved.documentNumberHash().orElseThrow())).isTrue();
    assertThat(accountRepository.findByEmail(saved.email())).isPresent();
    assertThat(accountRepository.findByHandle(saved.handle())).isPresent();
  }

  @Test
  void adminAccountPersistsWithoutDocumentFields() {
    Instant now = Instant.now();
    Account admin =
        Account.register(
            AccountId.generate(),
            AccountType.ADMIN,
            "Platform Admin",
            "admin-" + System.nanoTime(),
            "admin-" + System.nanoTime() + "@example.com",
            "hashed-password",
            null,
            null,
            null,
            now);

    Account saved = accountRepository.save(admin);

    assertThat(accountRepository.findById(saved.id()))
        .isPresent()
        .get()
        .satisfies(found -> assertThat(found.documentType()).isEmpty());
  }
}
