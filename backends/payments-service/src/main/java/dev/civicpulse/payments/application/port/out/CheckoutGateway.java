package dev.civicpulse.payments.application.port.out;

import dev.civicpulse.payments.domain.model.PaymentIntent;

/** Anti-corruption-layer boundary for the real payment gateway (Asaas) — see
 * AsaasCheckoutGatewayAdapter for the implementation. Asaas's own hosted invoice page collects
 * whatever the {@code billingType} needs (Pix QR/copy-paste, boleto barcode, or a card form), so
 * raw card data never reaches this backend or the CivicPulse frontend. */
public interface CheckoutGateway {

  CheckoutResult createPayment(PaymentIntent intent, PayerLookupGateway.PayerInfo payer);

  record CheckoutResult(String invoiceUrl, String externalPaymentId) {}
}
