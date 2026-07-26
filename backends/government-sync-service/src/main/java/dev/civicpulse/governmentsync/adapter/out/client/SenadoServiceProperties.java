package dev.civicpulse.governmentsync.adapter.out.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "government-sync.senado")
public record SenadoServiceProperties(String baseUrl) {}
