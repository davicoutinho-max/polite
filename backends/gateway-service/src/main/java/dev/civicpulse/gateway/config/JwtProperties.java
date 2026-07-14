package dev.civicpulse.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** {@code signingKeyBase64} must be byte-for-byte identical to identity-service's
 * {@code identity.jwt.signing-key-base64} — same env var name so the two can never drift apart. */
@ConfigurationProperties(prefix = "identity.jwt")
public record JwtProperties(String signingKeyBase64) {}
