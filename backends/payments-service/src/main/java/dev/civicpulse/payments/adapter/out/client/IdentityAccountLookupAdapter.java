package dev.civicpulse.payments.adapter.out.client;

import dev.civicpulse.payments.application.port.out.PayerLookupGateway;
import dev.civicpulse.payments.domain.exception.PaymentGatewayUnavailableException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Calls identity-service's internal-only {@code GET /accounts/{id}/payment-profile} directly
 * (bypassing the Gateway, which explicitly blocks this path — see identity-service's
 * RouteConfig) to get the real name/CPF-CNPJ a payment gateway customer record requires. */
@Component
class IdentityAccountLookupAdapter implements PayerLookupGateway {

  private static final Logger log = LoggerFactory.getLogger(IdentityAccountLookupAdapter.class);

  private final RestClient restClient;

  IdentityAccountLookupAdapter(RestClient.Builder restClientBuilder, PaymentsIdentityServiceProperties properties) {
    this.restClient = restClientBuilder.baseUrl(properties.baseUrl()).build();
  }

  @Override
  public PayerInfo getPaymentProfile(UUID accountId) {
    try {
      IdentityPaymentProfileResponse response =
          restClient.get().uri("/accounts/{id}/payment-profile", accountId).retrieve().body(IdentityPaymentProfileResponse.class);
      if (response == null) {
        throw new PaymentGatewayUnavailableException("identity-service returned no payment profile for account " + accountId);
      }
      return new PayerInfo(response.name(), response.documentNumber());
    } catch (RestClientException e) {
      log.warn("Failed to fetch payment profile for account {}: {}", accountId, e.getMessage());
      throw new PaymentGatewayUnavailableException(
          "Could not resolve the payer's identity for this payment — please try again shortly", e);
    }
  }

  /** Local shape of identity-service's AccountPaymentProfileResponse. */
  private record IdentityPaymentProfileResponse(String name, String documentNumber) {}
}
