package dev.civicpulse.payments.adapter.out.gateway;

import dev.civicpulse.payments.application.port.out.CheckoutGateway;
import dev.civicpulse.payments.application.port.out.PayerLookupGateway.PayerInfo;
import dev.civicpulse.payments.domain.exception.PaymentGatewayUnavailableException;
import dev.civicpulse.payments.domain.model.PaymentGatewayType;
import dev.civicpulse.payments.domain.model.PaymentIntent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Real Asaas integration — Pix, Boleto and Card all go through the same
 * find-or-create-customer-then-create-payment flow, differing only in {@code billingType}. Asaas's
 * own hosted {@code invoiceUrl} collects whatever the billing type needs (Pix QR/copy-paste,
 * boleto barcode, or a card form), so no raw card data ever reaches this backend. Until a real API
 * key is configured (see AsaasProperties), this throws {@link PaymentGatewayUnavailableException}
 * rather than silently failing or faking a success. */
@Component
class AsaasCheckoutGatewayAdapter implements CheckoutGateway {

  private static final Logger log = LoggerFactory.getLogger(AsaasCheckoutGatewayAdapter.class);

  // Boleto/Pix invoices need a due date even though Pix and card are typically settled
  // instantly — this only bounds how long Asaas keeps the invoice open for payment.
  private static final int DUE_IN_DAYS = 3;

  private final RestClient restClient;
  private final AsaasProperties properties;

  AsaasCheckoutGatewayAdapter(RestClient.Builder restClientBuilder, AsaasProperties properties) {
    this.restClient = restClientBuilder.baseUrl(properties.baseUrl()).build();
    this.properties = properties;
  }

  @Override
  public CheckoutResult createPayment(PaymentIntent intent, PayerInfo payer) {
    requireApiKey();
    try {
      String customerId = findOrCreateCustomer(payer);
      CreatePaymentRequest request =
          new CreatePaymentRequest(
              customerId,
              billingTypeOf(intent.gateway()),
              intent.amountCents() / 100.0,
              LocalDate.now().plusDays(DUE_IN_DAYS).format(DateTimeFormatter.ISO_LOCAL_DATE),
              describePurpose(intent),
              intent.id().toString());
      AsaasPaymentResponse response =
          restClient.post().uri("/payments").header("access_token", properties.apiKey()).body(request).retrieve().body(AsaasPaymentResponse.class);
      if (response == null) {
        throw new PaymentGatewayUnavailableException("Asaas returned no payment response");
      }
      return new CheckoutResult(response.invoiceUrl(), response.id());
    } catch (RestClientException e) {
      log.warn("Asaas payment creation failed: {}", e.getMessage());
      throw new PaymentGatewayUnavailableException("Could not start the payment — please try again shortly", e);
    }
  }

  private String findOrCreateCustomer(PayerInfo payer) {
    AsaasCustomerListResponse existing =
        restClient
            .get()
            .uri(uriBuilder -> uriBuilder.path("/customers").queryParam("cpfCnpj", payer.documentNumber()).build())
            .header("access_token", properties.apiKey())
            .retrieve()
            .body(AsaasCustomerListResponse.class);
    if (existing != null && existing.data() != null && !existing.data().isEmpty()) {
      return existing.data().get(0).id();
    }
    AsaasCustomerResponse created =
        restClient
            .post()
            .uri("/customers")
            .header("access_token", properties.apiKey())
            .body(new CreateCustomerRequest(payer.name(), payer.documentNumber()))
            .retrieve()
            .body(AsaasCustomerResponse.class);
    if (created == null) {
      throw new PaymentGatewayUnavailableException("Asaas returned no customer response");
    }
    return created.id();
  }

  private void requireApiKey() {
    if (properties.apiKey() == null || properties.apiKey().isBlank()) {
      throw new PaymentGatewayUnavailableException(
          "Asaas is not configured — copy payments-service/.env.example to .env and set a real ASAAS_API_KEY");
    }
  }

  private static String billingTypeOf(PaymentGatewayType gateway) {
    return switch (gateway) {
      case PIX -> "PIX";
      case CARD -> "CREDIT_CARD";
      case BOLETO -> "BOLETO";
    };
  }

  private static String describePurpose(PaymentIntent intent) {
    return switch (intent.purpose()) {
      case MEMBERSHIP_FEE -> "CivicPulse membership fee";
      case FUNDRAISING_CONTRIBUTION -> "CivicPulse fundraising contribution";
    };
  }

  private record CreateCustomerRequest(String name, String cpfCnpj) {}

  private record AsaasCustomerResponse(String id) {}

  private record AsaasCustomerListResponse(List<AsaasCustomerResponse> data) {}

  private record CreatePaymentRequest(
      String customer, String billingType, double value, String dueDate, String description, String externalReference) {}

  private record AsaasPaymentResponse(String id, String invoiceUrl, String status) {}
}
