package dev.civicpulse.participation.adapter.out.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** {@code apiKey} is deliberately never given a real default — it's read from the {@code
 * RESEND_API_KEY} environment variable (see participation-service/.env, gitignored) so the real
 * credential never lands in a committed file. See ResendEmailClient's javadoc for what happens
 * when it's blank. {@code fromAddress} must be a domain verified in the Resend dashboard. */
@ConfigurationProperties(prefix = "participation.email")
public record EmailProperties(String apiKey, String fromAddress) {}
