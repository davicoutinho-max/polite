package dev.civicpulse.fundraising.adapter.out.client;

import dev.civicpulse.fundraising.application.port.out.PaymentIntentLookupGateway;
import dev.civicpulse.fundraising.domain.exception.PaymentIntentLookupException;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
class PaymentIntentLookupAdapter implements PaymentIntentLookupGateway {

  private final RestClient restClient;

  PaymentIntentLookupAdapter(RestClient.Builder restClientBuilder, PaymentsServiceProperties properties) {
    this.restClient = restClientBuilder.baseUrl(properties.baseUrl()).build();
  }

  @Override
  public PaymentIntentSummary lookup(UUID paymentIntentId) {
    try {
      PaymentIntentResponse response = restClient.get().uri("/payment-intents/{id}", paymentIntentId).retrieve().body(PaymentIntentResponse.class);
      if (response == null) {
        throw new PaymentIntentLookupException("Payments Service returned an empty response for payment intent " + paymentIntentId, null);
      }
      return new PaymentIntentSummary(response.payerAccountId(), response.amountCents());
    } catch (RestClientResponseException e) {
      throw new PaymentIntentLookupException(
          "Payments Service rejected lookup of payment intent " + paymentIntentId + ": " + e.getResponseBodyAsString(), e);
    } catch (RestClientException e) {
      throw new PaymentIntentLookupException("Payments Service unreachable while looking up payment intent " + paymentIntentId, e);
    }
  }

  private record PaymentIntentResponse(UUID id, UUID payerAccountId, long amountCents) {}
}
