package dev.civicpulse.privacycompliance.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.civicpulse.privacycompliance.application.port.out.AccountDeletionRequestRepository;
import dev.civicpulse.privacycompliance.application.port.out.ErasureAuditLogRepository;
import dev.civicpulse.privacycompliance.application.port.out.EventPublisher;
import dev.civicpulse.privacycompliance.domain.model.AccountDeletionRequest;
import dev.civicpulse.privacycompliance.domain.model.DeletionStatus;
import dev.civicpulse.privacycompliance.domain.model.ErasureAuditEntry;
import dev.civicpulse.privacycompliance.domain.model.ExpectedErasureServices;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccountDeletionServiceTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Mock private AccountDeletionRequestRepository accountDeletionRequestRepository;
  @Mock private ErasureAuditLogRepository erasureAuditLogRepository;
  @Mock private EventPublisher eventPublisher;

  private AccountDeletionService service;

  @BeforeEach
  void setUp() {
    service = new AccountDeletionService(accountDeletionRequestRepository, erasureAuditLogRepository, eventPublisher, Clock.fixed(NOW, ZoneOffset.UTC));
  }

  @Test
  void onErasureReportedSkipsWhenAlreadyProcessed() {
    UUID deletionRequestId = UUID.randomUUID();
    when(erasureAuditLogRepository.existsByDeletionRequestIdAndServiceName(deletionRequestId, "feed-content-service")).thenReturn(true);

    service.onErasureReported(deletionRequestId, "feed-content-service", 10);

    verify(accountDeletionRequestRepository, never()).findById(any());
  }

  @Test
  void onErasureReportedRecordsButDoesNotCompleteUntilAllServicesReport() {
    UUID deletionRequestId = UUID.randomUUID();
    AccountDeletionRequest request = AccountDeletionRequest.request(deletionRequestId, UUID.randomUUID(), NOW);
    request.confirm();
    request.startProcessing();
    when(erasureAuditLogRepository.existsByDeletionRequestIdAndServiceName(deletionRequestId, "identity-service")).thenReturn(false);
    when(accountDeletionRequestRepository.findById(deletionRequestId)).thenReturn(Optional.of(request));
    when(erasureAuditLogRepository.findByDeletionRequestId(deletionRequestId))
        .thenReturn(List.of(ErasureAuditEntry.record(deletionRequestId, "identity-service", 1, NOW)));

    service.onErasureReported(deletionRequestId, "identity-service", 1);

    assertThat(request.status()).isEqualTo(DeletionStatus.PROCESSING);
    verify(accountDeletionRequestRepository, never()).save(any());
  }

  @Test
  void onErasureReportedCompletesOnceAllExpectedServicesReport() {
    UUID deletionRequestId = UUID.randomUUID();
    AccountDeletionRequest request = AccountDeletionRequest.request(deletionRequestId, UUID.randomUUID(), NOW);
    request.confirm();
    request.startProcessing();
    String lastService = ExpectedErasureServices.ALL.iterator().next();
    when(erasureAuditLogRepository.existsByDeletionRequestIdAndServiceName(deletionRequestId, lastService)).thenReturn(false);
    when(accountDeletionRequestRepository.findById(deletionRequestId)).thenReturn(Optional.of(request));

    List<ErasureAuditEntry> allReported = new ArrayList<>();
    for (String svc : ExpectedErasureServices.ALL) {
      allReported.add(ErasureAuditEntry.record(deletionRequestId, svc, 1, NOW));
    }
    when(erasureAuditLogRepository.findByDeletionRequestId(deletionRequestId)).thenReturn(allReported);

    service.onErasureReported(deletionRequestId, lastService, 1);

    assertThat(request.status()).isEqualTo(DeletionStatus.COMPLETED);
    verify(accountDeletionRequestRepository).save(request);
  }
}
