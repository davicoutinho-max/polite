package dev.civicpulse.elections.adapter.out.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "elections.directory-service")
public record DirectoryServiceProperties(String baseUrl) {}
