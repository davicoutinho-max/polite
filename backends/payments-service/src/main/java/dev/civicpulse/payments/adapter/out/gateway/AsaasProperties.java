package dev.civicpulse.payments.adapter.out.gateway;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** {@code apiKey}/{@code webhookToken} are deliberately never given real defaults — they're read
 * from the {@code ASAAS_API_KEY}/{@code ASAAS_WEBHOOK_TOKEN} environment variables (see
 * payments-service/.env, gitignored) so the real credentials never land in a committed file.
 * {@code baseUrl} defaults to Asaas's sandbox environment — switch to
 * {@code https://api.asaas.com/v3} only once genuinely ready to move real money. See
 * AsaasCheckoutGatewayAdapter/AsaasWebhookController's javadoc for what happens when they're
 * blank. */
@ConfigurationProperties(prefix = "payments.asaas")
public record AsaasProperties(String apiKey, String baseUrl, String webhookToken) {}
