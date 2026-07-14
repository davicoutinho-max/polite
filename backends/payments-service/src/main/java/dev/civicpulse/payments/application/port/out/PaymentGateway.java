package dev.civicpulse.payments.application.port.out;

import dev.civicpulse.payments.domain.model.PaymentGatewayType;

/** Anti-corruption-layer boundary for the real Pix/Card/Boleto provider integration — see
 * identity-service's DocumentVerificationGateway for the same stub pattern. A real
 * implementation would call out to the actual gateway's authorize/capture APIs; the stub
 * adapter always succeeds instantly. */
public interface PaymentGateway {

  AuthorizationResult authorize(PaymentGatewayType gateway, long amountCents, String idempotencyKey);

  record AuthorizationResult(boolean approved, String gatewayRef) {}
}
