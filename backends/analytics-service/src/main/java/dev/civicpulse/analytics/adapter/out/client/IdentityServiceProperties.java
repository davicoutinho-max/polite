package dev.civicpulse.analytics.adapter.out.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "analytics.identity-service")
public record IdentityServiceProperties(String baseUrl) {}
