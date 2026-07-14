package dev.civicpulse.partymanagement.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.civicpulse.partymanagement.domain.exception.AffiliationRequestNotPendingException;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AffiliationRequestTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Test
  void createStartsAsPending() {
    AffiliationRequest request = AffiliationRequest.create(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "São Paulo", NOW);

    assertThat(request.status()).isEqualTo(AffiliationRequestStatus.PENDING);
    assertThat(request.decidedAt()).isEmpty();
  }

  @Test
  void approveSetsStatusAndDecidedAt() {
    AffiliationRequest request = AffiliationRequest.create(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), null, NOW);
    Instant decidedAt = NOW.plusSeconds(60);

    request.approve(decidedAt);

    assertThat(request.status()).isEqualTo(AffiliationRequestStatus.APPROVED);
    assertThat(request.decidedAt()).contains(decidedAt);
  }

  @Test
  void rejectSetsStatusAndDecidedAt() {
    AffiliationRequest request = AffiliationRequest.create(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), null, NOW);
    Instant decidedAt = NOW.plusSeconds(60);

    request.reject(decidedAt);

    assertThat(request.status()).isEqualTo(AffiliationRequestStatus.REJECTED);
    assertThat(request.decidedAt()).contains(decidedAt);
  }

  @Test
  void cannotApproveAlreadyDecidedRequest() {
    AffiliationRequest request = AffiliationRequest.create(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), null, NOW);
    request.approve(NOW.plusSeconds(60));

    assertThatThrownBy(() -> request.approve(NOW.plusSeconds(120))).isInstanceOf(AffiliationRequestNotPendingException.class);
  }

  @Test
  void cannotRejectAlreadyDecidedRequest() {
    AffiliationRequest request = AffiliationRequest.create(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), null, NOW);
    request.reject(NOW.plusSeconds(60));

    assertThatThrownBy(() -> request.reject(NOW.plusSeconds(120))).isInstanceOf(AffiliationRequestNotPendingException.class);
  }
}
