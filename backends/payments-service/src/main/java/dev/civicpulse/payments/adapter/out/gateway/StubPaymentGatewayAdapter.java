package dev.civicpulse.payments.adapter.out.gateway;

import dev.civicpulse.payments.application.port.out.PaymentGateway;
import dev.civicpulse.payments.domain.model.PaymentGatewayType;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Anti-corruption-layer stub — see identity-service's StubDocumentVerificationGatewayAdapter
 * for the same pattern. Always approves instantly; a real adapter would call out to the actual
 * Pix/Card/Boleto provider's authorize API. */
@Component
class StubPaymentGatewayAdapter implements PaymentGateway {

  @Override
  public AuthorizationResult authorize(PaymentGatewayType gateway, long amountCents, String idempotencyKey) {
    return new AuthorizationResult(true, gateway.code() + "-" + UUID.randomUUID());
  }
}
