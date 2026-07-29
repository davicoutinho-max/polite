package dev.civicpulse.assistant.adapter.out.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** {@code apiKey} is deliberately never given a real default — it's read from the {@code
 * GEMINI_API_KEY} environment variable (see assistant-service/.env, gitignored) so the real
 * credential never lands in a committed file. See GeminiApiClient's javadoc for what happens
 * when it's blank. */
@ConfigurationProperties(prefix = "assistant.gemini")
public record GeminiProperties(String apiKey, String model, String baseUrl) {}
