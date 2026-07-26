package dev.civicpulse.governmentsync.adapter.out.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "government-sync.party-management-service")
public record PartyManagementServiceProperties(String baseUrl) {}
