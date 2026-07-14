package dev.civicpulse.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** One base URI per downstream service, each independently overridable via env var — mirrors
 * the {@code ${VAR:default}} convention used in every service's own application.yml. Defaults
 * point at localhost dev ports (see backends/README.md's port table). */
@ConfigurationProperties(prefix = "gateway.routes")
public record ServiceUris(
    String identityUri,
    String directoryUri,
    String partyManagementUri,
    String platformUri,
    String membershipUri,
    String paymentsUri,
    String feedUri,
    String liveUri,
    String fundraisingUri,
    String electionsUri,
    String participationUri,
    String messagingUri,
    String notificationUri,
    String privacyUri,
    String legislativeUri,
    String activityFeedUri,
    String analyticsUri,
    String assistantUri) {}
