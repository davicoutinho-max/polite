package dev.civicpulse.fundraising;

import dev.civicpulse.fundraising.adapter.out.client.PaymentsServiceProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Scope note: {@code contributions} rows (and the {@code raised_cents}/{@code supporters_count}
 * counters they drive) are written only in reaction to payments-service's real, durable {@code
 * PaymentCaptured} event — never optimistically at "contribute" request time — so the public
 * progress ledger can never show money that wasn't actually captured. Because that event carries
 * the payer's account id only opaquely (as a payment-intent id), this service makes one genuine
 * synchronous call back to payments-service (see PaymentIntentLookupGateway) to recover it, the
 * same anti-corruption-layer pattern party-management-service uses against identity-service.
 */
@SpringBootApplication
@EnableConfigurationProperties(PaymentsServiceProperties.class)
public class FundraisingServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(FundraisingServiceApplication.class, args);
  }
}
