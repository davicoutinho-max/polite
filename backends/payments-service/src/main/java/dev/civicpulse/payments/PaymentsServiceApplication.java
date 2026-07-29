package dev.civicpulse.payments;

import dev.civicpulse.payments.adapter.out.client.PaymentsIdentityServiceProperties;
import dev.civicpulse.payments.adapter.out.gateway.AsaasProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({AsaasProperties.class, PaymentsIdentityServiceProperties.class})
public class PaymentsServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(PaymentsServiceApplication.class, args);
  }
}
