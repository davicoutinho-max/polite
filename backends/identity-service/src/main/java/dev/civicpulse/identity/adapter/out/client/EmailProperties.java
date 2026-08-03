package dev.civicpulse.identity.adapter.out.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Mirrors participation-service's identically-shaped properties record — see its javadoc.
 * {@code registrationBaseUrl} is the Angular register page the invite link points to. */
@ConfigurationProperties(prefix = "identity.email")
public record EmailProperties(String apiKey, String fromAddress, String registrationBaseUrl) {}
