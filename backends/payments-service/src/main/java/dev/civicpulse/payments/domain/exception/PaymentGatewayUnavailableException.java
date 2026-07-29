package dev.civicpulse.payments.domain.exception;

/** Thrown when a real gateway call (Asaas, or the identity-service lookup it depends on) can't be
 * attempted or fails — either because no API key/webhook token is configured yet, or the gateway
 * itself rejected the request. Distinct from {@link InvalidPaymentTransitionException} (a
 * domain-state error) since this is an external dependency failure. */
public class PaymentGatewayUnavailableException extends RuntimeException {

  public PaymentGatewayUnavailableException(String message) {
    super(message);
  }

  public PaymentGatewayUnavailableException(String message, Throwable cause) {
    super(message, cause);
  }
}
