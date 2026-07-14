package dev.civicpulse.directory.adapter.out.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "directory.identity-service")
public record IdentityServiceProperties(String baseUrl) {}
