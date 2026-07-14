package dev.civicpulse.privacycompliance.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.civicpulse.privacycompliance.application.port.out.DataExportRequestRepository;
import dev.civicpulse.privacycompliance.application.port.out.EventPublisher;
import dev.civicpulse.privacycompliance.domain.event.DataExportRequested;
import dev.civicpulse.privacycompliance.domain.exception.DataExportRequestNotFoundException;
import dev.civicpulse.privacycompliance.domain.model.DataExportRequest;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DataExportServiceTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Mock private DataExportRequestRepository dataExportRequestRepository;
  @Mock private EventPublisher eventPublisher;

  private DataExportService service;

  @BeforeEach
  void setUp() {
    service = new DataExportService(dataExportRequestRepository, eventPublisher, Clock.fixed(NOW, ZoneOffset.UTC));
  }

  @Test
  void requestSavesAndPublishesEvent() {
    when(dataExportRequestRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    UUID accountId = UUID.randomUUID();

    DataExportRequest result = service.request(accountId);

    assertThat(result.accountId()).isEqualTo(accountId);
    verify(eventPublisher).publish(any(DataExportRequested.class));
  }

  @Test
  void getByIdThrowsWhenMissing() {
    UUID id = UUID.randomUUID();
    when(dataExportRequestRepository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.getById(id)).isInstanceOf(DataExportRequestNotFoundException.class);
  }

  @Test
  void startProcessingThenMarkReadyTransitionsCorrectly() {
    UUID id = UUID.randomUUID();
    DataExportRequest request = DataExportRequest.request(id, UUID.randomUUID(), NOW);
    when(dataExportRequestRepository.findById(id)).thenReturn(Optional.of(request));
    when(dataExportRequestRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    service.startProcessing(id);
    DataExportRequest result = service.markReady(id, "http://url", NOW.plusSeconds(3600));

    assertThat(result.downloadUrl()).contains("http://url");
  }
}
