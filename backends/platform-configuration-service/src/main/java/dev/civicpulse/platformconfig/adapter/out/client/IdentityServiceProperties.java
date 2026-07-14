package dev.civicpulse.platformconfig.adapter.out.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "platform-configuration.identity-service")
public record IdentityServiceProperties(String baseUrl) {}
