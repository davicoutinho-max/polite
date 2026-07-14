package dev.civicpulse.privacycompliance.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.civicpulse.privacycompliance.domain.exception.InvalidDeletionTransitionException;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AccountDeletionRequestTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Test
  void requestStartsPending() {
    AccountDeletionRequest request = AccountDeletionRequest.request(UUID.randomUUID(), UUID.randomUUID(), NOW);

    assertThat(request.status()).isEqualTo(DeletionStatus.PENDING);
  }

  @Test
  void fullHappyPathTransition() {
    AccountDeletionRequest request = AccountDeletionRequest.request(UUID.randomUUID(), UUID.randomUUID(), NOW);

    request.confirm();
    assertThat(request.status()).isEqualTo(DeletionStatus.CONFIRMED);

    request.startProcessing();
    assertThat(request.status()).isEqualTo(DeletionStatus.PROCESSING);

    request.complete(NOW);
    assertThat(request.status()).isEqualTo(DeletionStatus.COMPLETED);
    assertThat(request.completedAt()).contains(NOW);
  }

  @Test
  void cancelAllowedFromPendingOrConfirmed() {
    AccountDeletionRequest fromPending = AccountDeletionRequest.request(UUID.randomUUID(), UUID.randomUUID(), NOW);
    fromPending.cancel();
    assertThat(fromPending.status()).isEqualTo(DeletionStatus.CANCELED);

    AccountDeletionRequest fromConfirmed = AccountDeletionRequest.request(UUID.randomUUID(), UUID.randomUUID(), NOW);
    fromConfirmed.confirm();
    fromConfirmed.cancel();
    assertThat(fromConfirmed.status()).isEqualTo(DeletionStatus.CANCELED);
  }

  @Test
  void cancelRejectedOnceProcessing() {
    AccountDeletionRequest request = AccountDeletionRequest.request(UUID.randomUUID(), UUID.randomUUID(), NOW);
    request.confirm();
    request.startProcessing();

    assertThatThrownBy(request::cancel).isInstanceOf(InvalidDeletionTransitionException.class);
  }

  @Test
  void completeRequiresProcessingStatus() {
    AccountDeletionRequest request = AccountDeletionRequest.request(UUID.randomUUID(), UUID.randomUUID(), NOW);

    assertThatThrownBy(() -> request.complete(NOW)).isInstanceOf(InvalidDeletionTransitionException.class);
  }
}
