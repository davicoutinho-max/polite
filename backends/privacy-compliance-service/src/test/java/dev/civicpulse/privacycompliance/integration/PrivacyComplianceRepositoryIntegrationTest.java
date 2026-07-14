package dev.civicpulse.privacycompliance.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import dev.civicpulse.privacycompliance.application.port.out.AccountDeletionRequestRepository;
import dev.civicpulse.privacycompliance.application.port.out.ConsentRecordRepository;
import dev.civicpulse.privacycompliance.application.port.out.DataExportRequestRepository;
import dev.civicpulse.privacycompliance.application.port.out.ErasureAuditLogRepository;
import dev.civicpulse.privacycompliance.domain.model.AccountDeletionRequest;
import dev.civicpulse.privacycompliance.domain.model.ConsentPurpose;
import dev.civicpulse.privacycompliance.domain.model.ConsentRecord;
import dev.civicpulse.privacycompliance.domain.model.DataExportRequest;
import dev.civicpulse.privacycompliance.domain.model.ErasureAuditEntry;
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
      "spring.datasource.url=jdbc:postgresql://localhost:5432/privacy_compliance_service",
      "spring.datasource.username=privacy_compliance_service_app",
      "spring.datasource.password=privacy_dev_pw"
    })
class PrivacyComplianceRepositoryIntegrationTest {

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

  @Autowired private ConsentRecordRepository consentRecordRepository;
  @Autowired private DataExportRequestRepository dataExportRequestRepository;
  @Autowired private AccountDeletionRequestRepository accountDeletionRequestRepository;
  @Autowired private ErasureAuditLogRepository erasureAuditLogRepository;

  @Test
  void consentRecordRoundTripAndUpdate() {
    UUID accountId = UUID.randomUUID();
    consentRecordRepository.save(ConsentRecord.record(accountId, ConsentPurpose.ANALYTICS, true, Instant.now()));

    assertThat(consentRecordRepository.findByAccountAndPurpose(accountId, ConsentPurpose.ANALYTICS))
        .isPresent()
        .get()
        .satisfies(found -> assertThat(found.granted()).isTrue());

    ConsentRecord existing = consentRecordRepository.findByAccountAndPurpose(accountId, ConsentPurpose.ANALYTICS).orElseThrow();
    existing.update(false, Instant.now());
    consentRecordRepository.save(existing);

    assertThat(consentRecordRepository.findByAccountAndPurpose(accountId, ConsentPurpose.ANALYTICS))
        .isPresent()
        .get()
        .satisfies(found -> assertThat(found.granted()).isFalse());
  }

  @Test
  void dataExportRequestRoundTrip() {
    UUID id = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    dataExportRequestRepository.save(DataExportRequest.request(id, accountId, Instant.now()));

    assertThat(dataExportRequestRepository.findById(id)).isPresent();
    assertThat(dataExportRequestRepository.findByAccountId(accountId)).extracting(DataExportRequest::id).contains(id);
  }

  @Test
  void accountDeletionRequestAndErasureAuditRoundTrip() {
    UUID id = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    accountDeletionRequestRepository.save(AccountDeletionRequest.request(id, accountId, Instant.now()));

    assertThat(erasureAuditLogRepository.existsByDeletionRequestIdAndServiceName(id, "identity-service")).isFalse();

    erasureAuditLogRepository.save(ErasureAuditEntry.record(id, "identity-service", 5, Instant.now()));

    assertThat(erasureAuditLogRepository.existsByDeletionRequestIdAndServiceName(id, "identity-service")).isTrue();
    assertThat(erasureAuditLogRepository.findByDeletionRequestId(id)).anySatisfy(e -> assertThat(e.serviceName()).isEqualTo("identity-service"));
  }
}
