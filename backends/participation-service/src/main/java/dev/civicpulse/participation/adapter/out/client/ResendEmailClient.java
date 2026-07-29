package dev.civicpulse.participation.adapter.out.client;

import dev.civicpulse.participation.application.port.out.EmailGateway;
import dev.civicpulse.participation.domain.exception.EmailDeliveryException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Resend (resend.com) transactional email — the real channel a petition signature's verification
 * code is sent through. {@code RESEND_API_KEY}/{@code RESEND_FROM_ADDRESS} are read from the
 * environment (see EmailProperties' javadoc); with no key configured this always fails closed
 * (never falls back to returning the code in the API response, which was this flow's old, now-
 * removed, demo-only behavior — see git history for PendingSignatureVerification/
 * SignatureVerificationStarted). */
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
  public void sendVerificationCode(String toEmail, String code) {
    if (properties.apiKey() == null || properties.apiKey().isBlank()) {
      throw new EmailDeliveryException(
          "RESEND_API_KEY is not configured — copy participation-service/.env.example to .env and set a real key");
    }
    if (toEmail == null || toEmail.isBlank()) {
      throw new EmailDeliveryException("No email address was provided to send the verification code to");
    }
    String html =
        "<p>Your CivicPulse petition-signature verification code is:</p>"
            + "<p style=\"font-size:28px;font-weight:700;letter-spacing:4px;\">"
            + code
            + "</p>"
            + "<p>This code expires in 10 minutes. If you didn't request this, you can ignore this email.</p>";
    SendEmailRequest request =
        new SendEmailRequest(properties.fromAddress(), List.of(toEmail), "Your CivicPulse verification code: " + code, html);
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
      throw new EmailDeliveryException("Could not send the verification email — please try again shortly", e);
    }
  }

  private record SendEmailRequest(String from, List<String> to, String subject, String html) {}
}
