package dev.civicpulse.governmentsync.adapter.out.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "government-sync.tse")
public record TseServiceProperties(String baseUrl) {}
