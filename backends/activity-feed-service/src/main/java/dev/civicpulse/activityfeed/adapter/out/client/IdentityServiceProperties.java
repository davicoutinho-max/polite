package dev.civicpulse.activityfeed.adapter.out.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "activity-feed.identity-service")
public record IdentityServiceProperties(String baseUrl) {}
