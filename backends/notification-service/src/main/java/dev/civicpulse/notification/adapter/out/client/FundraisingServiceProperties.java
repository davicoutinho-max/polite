package dev.civicpulse.notification.adapter.out.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "notification.fundraising-service")
public record FundraisingServiceProperties(String baseUrl) {}
