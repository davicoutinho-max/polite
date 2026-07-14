package dev.civicpulse.fundraising.adapter.in.messaging;

import dev.civicpulse.fundraising.adapter.in.messaging.dto.PaymentCapturedMessage;
import dev.civicpulse.fundraising.application.port.in.ManageContributionUseCase;
import dev.civicpulse.fundraising.domain.exception.FundraiserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
class FundraisingProjectionListener {

  private static final Logger log = LoggerFactory.getLogger(FundraisingProjectionListener.class);

  private final ManageContributionUseCase manageContributionUseCase;

  FundraisingProjectionListener(ManageContributionUseCase manageContributionUseCase) {
    this.manageContributionUseCase = manageContributionUseCase;
  }

  /** {@code payment-captured} is a shared topic across every payment purpose (membership fees,
   * fundraising contributions, ...) — a {@code referenceId} that is null or doesn't resolve to
   * one of our own fundraisers simply means the captured payment was for someone else's purpose,
   * not a delivery failure, so it's skipped rather than retried. Without this guard, Spring
   * Kafka's default error handler re-delivers the same record with a "seek to current" retry loop
   * (visible as repeated ERROR logs) until it exhausts its backoff and moves on — correct
   * eventually, but noisy and slow compared to recognizing up front that the message isn't ours. */
  @KafkaListener(topics = "payment-captured", groupId = "fundraising-service")
  void onPaymentCaptured(PaymentCapturedMessage message) {
    if (message.referenceId() == null) {
      log.debug("Ignoring payment-captured with a null referenceId — not a fundraiser of ours");
      return;
    }
    try {
      manageContributionUseCase.onPaymentCaptured(message.referenceId(), message.paymentIntentId(), message.amountCents());
    } catch (FundraiserNotFoundException e) {
      log.debug("Ignoring payment-captured for referenceId {} — not a fundraiser of ours", message.referenceId());
    }
  }
}
