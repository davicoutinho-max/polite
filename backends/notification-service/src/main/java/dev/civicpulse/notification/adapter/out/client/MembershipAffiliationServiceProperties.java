package dev.civicpulse.notification.adapter.out.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "notification.membership-affiliation-service")
public record MembershipAffiliationServiceProperties(String baseUrl) {}
