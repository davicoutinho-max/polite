package dev.civicpulse.partymanagement.adapter.out.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "party-management.identity-service")
public record IdentityServiceProperties(String baseUrl) {}
