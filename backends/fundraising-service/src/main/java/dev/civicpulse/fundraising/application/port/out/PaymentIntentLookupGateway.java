package dev.civicpulse.fundraising.application.port.out;

import java.util.UUID;

/** Anti-corruption-layer port to payments-service — a real synchronous REST call (see
 * party-management-service's IdentityProvisioningGateway for the established pattern), used only
 * to recover the payer's account id, which {@code PaymentCaptured} carries opaquely and doesn't
 * include (see FundraisingServiceApplication's note). */
public interface PaymentIntentLookupGateway {

  PaymentIntentSummary lookup(UUID paymentIntentId);

  record PaymentIntentSummary(UUID payerAccountId, long amountCents) {}
}
