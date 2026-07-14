package dev.civicpulse.fundraising.application.port.in;

import dev.civicpulse.fundraising.domain.model.Contribution;
import java.util.List;
import java.util.UUID;

public interface ManageContributionUseCase {

  /** Reacts to payments-service's {@code PaymentCaptured} — idempotent under Kafka redelivery
   * (see FundraisingServiceApplication's note). {@code fundraiserId} is the event's opaque
   * {@code referenceId}. */
  void onPaymentCaptured(UUID fundraiserId, UUID paymentIntentId, long amountCents);

  /** Throws {@code LedgerNotPublicException} unless the fundraiser opted into a public ledger. */
  List<Contribution> listByFundraiser(UUID fundraiserId);
}
