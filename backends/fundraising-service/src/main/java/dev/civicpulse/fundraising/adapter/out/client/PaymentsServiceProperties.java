package dev.civicpulse.fundraising.adapter.out.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fundraising.payments-service")
public record PaymentsServiceProperties(String baseUrl) {}
