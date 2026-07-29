package dev.civicpulse.payments.application.port.in;

import dev.civicpulse.payments.domain.model.PaymentGatewayType;
import dev.civicpulse.payments.domain.model.PaymentIntent;
import dev.civicpulse.payments.domain.model.PaymentPurpose;
import java.util.UUID;

public interface ManagePaymentIntentUseCase {

  /** Every gateway (Pix, Card, Boleto) is real now (Asaas) — none of them can be synchronously
   * approved in this same call the way an old demo stub could, since the citizen still has to
   * complete payment on Asaas's own hosted invoice page. This only creates the intent row (status
   * stays CREATED); see {@link #createCheckoutUrl} for the next step and {@link #confirmPayment}
   * for how it eventually reaches CAPTURED. Idempotent: replaying the same {@code idempotencyKey}
   * returns the original intent rather than creating a duplicate charge. */
  PaymentIntent createPendingPayment(
      PaymentPurpose purpose, UUID referenceId, UUID payerAccountId, UUID payeeId, long amountCents, PaymentGatewayType gateway, String idempotencyKey);

  /** Returns the Asaas-hosted invoice URL the frontend should send the citizen to. */
  String createCheckoutUrl(UUID intentId);

  /** Called from the Asaas webhook once the invoice actually settles — the only trustworthy
   * confirmation (never assume success just because the citizen was sent to the invoice page).
   * Idempotent: a webhook retry for an already-captured intent is a no-op rather than
   * double-crediting the ledger. */
  PaymentIntent confirmPayment(UUID intentId, String externalPaymentId);

  PaymentIntent capture(UUID intentId);

  PaymentIntent refund(UUID intentId);

  PaymentIntent getById(UUID intentId);
}
