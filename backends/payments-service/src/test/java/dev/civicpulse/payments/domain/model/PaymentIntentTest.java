package dev.civicpulse.payments.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.civicpulse.payments.domain.exception.InvalidPaymentTransitionException;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PaymentIntentTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Test
  void createStartsInCreatedStatus() {
    PaymentIntent intent = createIntent();

    assertThat(intent.status()).isEqualTo(PaymentStatus.CREATED);
  }

  @Test
  void rejectsNonPositiveAmount() {
    assertThatThrownBy(
            () ->
                PaymentIntent.create(
                    UUID.randomUUID(), PaymentPurpose.MEMBERSHIP_FEE, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 0, "BRL",
                    PaymentGatewayType.PIX, "key-1", NOW))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void happyPathAdvancesThroughAuthorizeCaptureRefund() {
    PaymentIntent intent = createIntent();

    intent.authorize("gw-ref-1", NOW);
    assertThat(intent.status()).isEqualTo(PaymentStatus.AUTHORIZED);
    assertThat(intent.gatewayRef()).contains("gw-ref-1");

    intent.capture(NOW);
    assertThat(intent.status()).isEqualTo(PaymentStatus.CAPTURED);

    intent.refund(NOW);
    assertThat(intent.status()).isEqualTo(PaymentStatus.REFUNDED);
  }

  @Test
  void cannotCaptureWithoutAuthorizing() {
    PaymentIntent intent = createIntent();

    assertThatThrownBy(() -> intent.capture(NOW)).isInstanceOf(InvalidPaymentTransitionException.class);
  }

  @Test
  void cannotRefundWithoutCapturing() {
    PaymentIntent intent = createIntent();
    intent.authorize("gw-ref-1", NOW);

    assertThatThrownBy(() -> intent.refund(NOW)).isInstanceOf(InvalidPaymentTransitionException.class);
  }

  @Test
  void canFailFromCreatedOrAuthorizedButNotFromCaptured() {
    PaymentIntent fromCreated = createIntent();
    fromCreated.fail(NOW);
    assertThat(fromCreated.status()).isEqualTo(PaymentStatus.FAILED);

    PaymentIntent fromAuthorized = createIntent();
    fromAuthorized.authorize("gw-ref-1", NOW);
    fromAuthorized.fail(NOW);
    assertThat(fromAuthorized.status()).isEqualTo(PaymentStatus.FAILED);

    PaymentIntent fromCaptured = createIntent();
    fromCaptured.authorize("gw-ref-1", NOW);
    fromCaptured.capture(NOW);
    assertThatThrownBy(() -> fromCaptured.fail(NOW)).isInstanceOf(InvalidPaymentTransitionException.class);
  }

  @Test
  void canCancelOnlyFromCreated() {
    PaymentIntent intent = createIntent();
    intent.cancel(NOW);
    assertThat(intent.status()).isEqualTo(PaymentStatus.CANCELED);

    PaymentIntent authorized = createIntent();
    authorized.authorize("gw-ref-1", NOW);
    assertThatThrownBy(() -> authorized.cancel(NOW)).isInstanceOf(InvalidPaymentTransitionException.class);
  }

  private static PaymentIntent createIntent() {
    return PaymentIntent.create(
        UUID.randomUUID(),
        PaymentPurpose.MEMBERSHIP_FEE,
        UUID.randomUUID(),
        UUID.randomUUID(),
        UUID.randomUUID(),
        5000,
        "BRL",
        PaymentGatewayType.PIX,
        "key-" + UUID.randomUUID(),
        NOW);
  }
}
