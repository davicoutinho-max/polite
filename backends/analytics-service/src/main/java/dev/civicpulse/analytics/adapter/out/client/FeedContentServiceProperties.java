package dev.civicpulse.analytics.adapter.out.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "analytics.feed-content-service")
public record FeedContentServiceProperties(String baseUrl) {}
