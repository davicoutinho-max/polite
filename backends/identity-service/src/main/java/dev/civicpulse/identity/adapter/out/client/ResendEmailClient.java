package dev.civicpulse.identity.adapter.out.client;

import dev.civicpulse.identity.application.port.out.EmailGateway;
import dev.civicpulse.identity.domain.exception.EmailDeliveryException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Resend (resend.com) transactional email — mirrors participation-service's ResendEmailClient
 * exactly (same env vars, same fail-closed behavior with no key configured). */
@Component
class ResendEmailClient implements EmailGateway {

  private static final Logger log = LoggerFactory.getLogger(ResendEmailClient.class);
  private static final String API_BASE = "https://api.resend.com";

  private final RestClient restClient;
  private final EmailProperties properties;

  ResendEmailClient(RestClient.Builder restClientBuilder, EmailProperties properties) {
    this.restClient = restClientBuilder.baseUrl(API_BASE).build();
    this.properties = properties;
  }

  @Override
  public void sendRegistrationInvite(String toEmail, String rawToken, String accountTypeLabel) {
    if (properties.apiKey() == null || properties.apiKey().isBlank()) {
      throw new EmailDeliveryException(
          "RESEND_API_KEY is not configured — copy identity-service/.env.example to .env and set a real key");
    }
    if (toEmail == null || toEmail.isBlank()) {
      throw new EmailDeliveryException("No email address was provided to send the invite to");
    }
    String registrationUrl = properties.registrationBaseUrl() + "?token=" + rawToken;
    String html =
        "<p>You've been invited to register a "
            + accountTypeLabel
            + " account on CivicPulse.</p>"
            + "<p><a href=\""
            + registrationUrl
            + "\">Click here to complete your registration</a></p>"
            + "<p>This link expires in 2 days. If you weren't expecting this, you can ignore this email.</p>";
    SendEmailRequest request =
        new SendEmailRequest(properties.fromAddress(), List.of(toEmail), "You're invited to CivicPulse", html);
    try {
      restClient
          .post()
          .uri("/emails")
          .header("Authorization", "Bearer " + properties.apiKey())
          .body(request)
          .retrieve()
          .toBodilessEntity();
    } catch (RestClientException e) {
      log.warn("Resend email send failed: {}", e.getMessage());
      throw new EmailDeliveryException("Could not send the invite email — please try again shortly", e);
    }
  }

  private record SendEmailRequest(String from, List<String> to, String subject, String html) {}
}
