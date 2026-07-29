package dev.civicpulse.payments.adapter.in.web;

import dev.civicpulse.payments.adapter.out.gateway.AsaasProperties;
import dev.civicpulse.payments.application.port.in.ManagePaymentIntentUseCase;
import dev.civicpulse.payments.domain.exception.PaymentGatewayUnavailableException;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Asaas calls this once a payment actually settles — the only trustworthy confirmation (never
 * assume success just because the citizen was sent to the invoice page). Verified against a
 * shared token (configured both here via {@code ASAAS_WEBHOOK_TOKEN} and in the Asaas dashboard's
 * webhook settings, sent back on every call as the {@code asaas-access-token} header) rather than
 * a cryptographic signature — Asaas's own webhook auth model, simpler than Stripe's HMAC but
 * still unguessable to an outside caller. This endpoint is public (Asaas itself calls it, not an
 * authenticated CivicPulse account) but forged calls are rejected at the token-check step.
 *
 * <p>Local dev: Asaas can't reach localhost directly — use a tunnel (e.g. ngrok) and register the
 * public URL + this same token in the Asaas sandbox dashboard's webhook settings. */
@RestController
@RequestMapping("/webhooks/asaas")
public class AsaasWebhookController {

  private static final Set<String> SETTLED_EVENTS = Set.of("PAYMENT_CONFIRMED", "PAYMENT_RECEIVED");

  private final ManagePaymentIntentUseCase managePaymentIntentUseCase;
  private final AsaasProperties properties;

  public AsaasWebhookController(ManagePaymentIntentUseCase managePaymentIntentUseCase, AsaasProperties properties) {
    this.managePaymentIntentUseCase = managePaymentIntentUseCase;
    this.properties = properties;
  }

  @PostMapping
  public ResponseEntity<Void> handle(@RequestBody AsaasWebhookEvent event, @RequestHeader("asaas-access-token") String token) {
    if (properties.webhookToken() == null || properties.webhookToken().isBlank()) {
      throw new PaymentGatewayUnavailableException("Asaas webhook token is not configured — set ASAAS_WEBHOOK_TOKEN in payments-service/.env");
    }
    if (!properties.webhookToken().equals(token)) {
      return ResponseEntity.status(401).build();
    }

    if (SETTLED_EVENTS.contains(event.event()) && event.payment() != null && event.payment().externalReference() != null) {
      managePaymentIntentUseCase.confirmPayment(UUID.fromString(event.payment().externalReference()), event.payment().id());
    }
    return ResponseEntity.ok().build();
  }

  private record AsaasWebhookEvent(String event, AsaasWebhookPayment payment) {}

  private record AsaasWebhookPayment(String id, String externalReference) {}
}
