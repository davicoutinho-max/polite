package dev.civicpulse.payments.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import dev.civicpulse.payments.application.port.out.LedgerEntryRepository;
import dev.civicpulse.payments.application.port.out.OutboxEventRepository;
import dev.civicpulse.payments.application.port.out.PaymentIntentRepository;
import dev.civicpulse.payments.domain.model.LedgerDirection;
import dev.civicpulse.payments.domain.model.LedgerEntry;
import dev.civicpulse.payments.domain.model.OutboxEvent;
import dev.civicpulse.payments.domain.model.PaymentGatewayType;
import dev.civicpulse.payments.domain.model.PaymentIntent;
import dev.civicpulse.payments.domain.model.PaymentPurpose;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** Exercises the real JPA/Postgres adapters against the shared local-dev database (see
 * identity-service's equivalent test for the rationale on why this isn't Testcontainers). */
@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:postgresql://localhost:5432/payments_service",
      "spring.datasource.username=payments_service_app",
      "spring.datasource.password=payments_dev_pw"
    })
class PaymentsRepositoryIntegrationTest {

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

  @Autowired private PaymentIntentRepository paymentIntentRepository;
  @Autowired private LedgerEntryRepository ledgerEntryRepository;
  @Autowired private OutboxEventRepository outboxEventRepository;

  @Test
  void savesAndRetrievesPaymentIntentByIdempotencyKey() {
    UUID id = UUID.randomUUID();
    String idempotencyKey = "key-" + System.nanoTime();
    PaymentIntent intent =
        PaymentIntent.create(
            id, PaymentPurpose.MEMBERSHIP_FEE, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 5000, "BRL", PaymentGatewayType.PIX,
            idempotencyKey, Instant.now());

    paymentIntentRepository.save(intent);

    assertThat(paymentIntentRepository.findByIdempotencyKey(idempotencyKey)).isPresent().get().satisfies(found -> assertThat(found.id()).isEqualTo(id));
  }

  @Test
  void ledgerEntryRunningBalanceAccumulatesPerAccount() {
    UUID accountId = UUID.randomUUID();
    UUID intentId = UUID.randomUUID();
    paymentIntentRepository.save(
        PaymentIntent.create(
            intentId, PaymentPurpose.MEMBERSHIP_FEE, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 5000, "BRL", PaymentGatewayType.PIX,
            "key-" + System.nanoTime(), Instant.now()));
    assertThat(ledgerEntryRepository.currentBalance(accountId)).isZero();

    ledgerEntryRepository.save(LedgerEntry.record(intentId, accountId, LedgerDirection.CREDIT, 5000, 5000, Instant.now()));
    assertThat(ledgerEntryRepository.currentBalance(accountId)).isEqualTo(5000);

    ledgerEntryRepository.save(LedgerEntry.record(intentId, accountId, LedgerDirection.DEBIT, 2000, 3000, Instant.now()));
    assertThat(ledgerEntryRepository.currentBalance(accountId)).isEqualTo(3000);
  }

  @Test
  void outboxEventPersistsJsonPayloadAndCanBeMarkedPublished() {
    UUID aggregateId = UUID.randomUUID();
    OutboxEvent event = OutboxEvent.record(UUID.randomUUID(), aggregateId, "PaymentCaptured", "{\"foo\":\"bar\"}", Instant.now());

    outboxEventRepository.save(event);

    assertThat(outboxEventRepository.findUnpublished(100)).anySatisfy(e -> assertThat(e.aggregateId()).isEqualTo(aggregateId));
  }
}
