package dev.civicpulse.legislative.adapter.out.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "legislative.assistant-service")
public record AssistantServiceProperties(String baseUrl) {}
