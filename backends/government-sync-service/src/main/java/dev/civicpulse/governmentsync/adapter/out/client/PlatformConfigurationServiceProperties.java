package dev.civicpulse.governmentsync.adapter.out.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "government-sync.platform-configuration-service")
public record PlatformConfigurationServiceProperties(String baseUrl) {}
