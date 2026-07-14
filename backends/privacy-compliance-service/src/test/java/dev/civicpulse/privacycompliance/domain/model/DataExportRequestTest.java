package dev.civicpulse.privacycompliance.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.civicpulse.privacycompliance.domain.exception.InvalidExportTransitionException;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DataExportRequestTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Test
  void requestStartsPending() {
    DataExportRequest request = DataExportRequest.request(UUID.randomUUID(), UUID.randomUUID(), NOW);

    assertThat(request.status()).isEqualTo(ExportStatus.PENDING);
  }

  @Test
  void fullHappyPathTransition() {
    DataExportRequest request = DataExportRequest.request(UUID.randomUUID(), UUID.randomUUID(), NOW);

    request.startProcessing();
    assertThat(request.status()).isEqualTo(ExportStatus.PROCESSING);

    request.markReady("http://download", NOW.plusSeconds(3600), NOW);
    assertThat(request.status()).isEqualTo(ExportStatus.READY);
    assertThat(request.downloadUrl()).contains("http://download");
  }

  @Test
  void markReadyRequiresProcessingStatus() {
    DataExportRequest request = DataExportRequest.request(UUID.randomUUID(), UUID.randomUUID(), NOW);

    assertThatThrownBy(() -> request.markReady("url", NOW, NOW)).isInstanceOf(InvalidExportTransitionException.class);
  }

  @Test
  void markFailedAllowedFromPendingOrProcessing() {
    DataExportRequest fromPending = DataExportRequest.request(UUID.randomUUID(), UUID.randomUUID(), NOW);
    fromPending.markFailed(NOW);
    assertThat(fromPending.status()).isEqualTo(ExportStatus.FAILED);

    DataExportRequest fromProcessing = DataExportRequest.request(UUID.randomUUID(), UUID.randomUUID(), NOW);
    fromProcessing.startProcessing();
    fromProcessing.markFailed(NOW);
    assertThat(fromProcessing.status()).isEqualTo(ExportStatus.FAILED);
  }

  @Test
  void markFailedRejectedFromReady() {
    DataExportRequest request = DataExportRequest.request(UUID.randomUUID(), UUID.randomUUID(), NOW);
    request.startProcessing();
    request.markReady("url", NOW, NOW);

    assertThatThrownBy(() -> request.markFailed(NOW)).isInstanceOf(InvalidExportTransitionException.class);
  }
}
