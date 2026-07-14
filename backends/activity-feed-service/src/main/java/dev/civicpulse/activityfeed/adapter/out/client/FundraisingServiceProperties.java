package dev.civicpulse.activityfeed.adapter.out.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "activity-feed.fundraising-service")
public record FundraisingServiceProperties(String baseUrl) {}
