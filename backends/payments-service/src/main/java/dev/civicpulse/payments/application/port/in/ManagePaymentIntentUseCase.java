package dev.civicpulse.payments.application.port.in;

import dev.civicpulse.payments.domain.model.PaymentGatewayType;
import dev.civicpulse.payments.domain.model.PaymentIntent;
import dev.civicpulse.payments.domain.model.PaymentPurpose;
import java.util.UUID;

public interface ManagePaymentIntentUseCase {

  /** Creates the intent and immediately attempts authorization via the gateway (see
   * PaymentGateway) in the same call — this demo's flows are all synchronous
   * request/response, not async webhook-driven. Idempotent: replaying the same {@code
   * idempotencyKey} returns the original intent rather than creating a duplicate charge. */
  PaymentIntent createAndAuthorize(
      PaymentPurpose purpose, UUID referenceId, UUID payerAccountId, UUID payeeId, long amountCents, PaymentGatewayType gateway, String idempotencyKey);

  PaymentIntent capture(UUID intentId);

  PaymentIntent refund(UUID intentId);

  PaymentIntent getById(UUID intentId);
}
