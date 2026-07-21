package dev.civicpulse.gateway.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

/**
 * External namespace is {@code /api/<service>/**} for every backend, stripped down to that
 * service's own real base path before forwarding (e.g. {@code /api/directory/politicians} →
 * {@code /politicians} on directory-service). This is deliberate, not cosmetic: several services
 * share the exact same bare path today (both directory-service and platform-configuration-service
 * expose {@code GET /parties}/{@code GET /parties/{id}} for entirely different things — the
 * public directory vs. the platform's party registry), so proxying raw un-namespaced paths would
 * silently collide. Namespacing by service at the gateway avoids that for every current and
 * future service, not just the one collision already found.
 *
 * <p>{@code /accounts/provision} (identity-service) must never be reachable through the gateway
 * — it provisions politician/party/admin accounts and is meant to be called only by other
 * backend services directly (see identity-service's {@code AccountController.provision}
 * javadoc). Because a {@code {id}}/{@code *} path-variable route segment matches the literal
 * string {@code "provision"} just as well as any real id, an explicit higher-priority "blocked"
 * route is registered first — Spring Cloud Gateway evaluates routes in registration order and
 * stops at the first predicate match, so this route always wins over the more general accounts
 * route registered after it.
 */
@Configuration
@EnableConfigurationProperties(ServiceUris.class)
public class RouteConfig {

  @Bean
  public RouteLocator routes(RouteLocatorBuilder builder, ServiceUris uris) {
    return builder
        .routes()
        // --- identity-service: /accounts/provision is structurally unreachable (see class javadoc) ---
        .route(
            "identity-accounts-provision-blocked",
            r ->
                r.path("/api/identity/accounts/provision")
                    .filters(
                        f ->
                            f.filter(
                                (exchange, chain) -> {
                                  exchange.getResponse().setStatusCode(HttpStatus.NOT_FOUND);
                                  return exchange.getResponse().setComplete();
                                }))
                    .uri(uris.identityUri()))
        .route(
            "identity-accounts",
            r ->
                r.path(
                        "/api/identity/accounts/register",
                        "/api/identity/accounts/*",
                        "/api/identity/accounts/*/permissions",
                        "/api/identity/accounts/*/verify-document")
                    .filters(f -> f.stripPrefix(2))
                    .uri(uris.identityUri()))
        .route("identity-auth", r -> r.path("/api/identity/auth/**").filters(f -> f.stripPrefix(2)).uri(uris.identityUri()))
        // --- the remaining 13 services: one straightforward prefix-strip route each ---
        .route("directory", r -> r.path("/api/directory/**").filters(f -> f.stripPrefix(2)).uri(uris.directoryUri()))
        .route("party-management", r -> r.path("/api/party-management/**").filters(f -> f.stripPrefix(2)).uri(uris.partyManagementUri()))
        .route("platform", r -> r.path("/api/platform/**").filters(f -> f.stripPrefix(2)).uri(uris.platformUri()))
        .route("membership", r -> r.path("/api/membership/**").filters(f -> f.stripPrefix(2)).uri(uris.membershipUri()))
        .route("payments", r -> r.path("/api/payments/**").filters(f -> f.stripPrefix(2)).uri(uris.paymentsUri()))
        .route("feed", r -> r.path("/api/feed/**").filters(f -> f.stripPrefix(2)).uri(uris.feedUri()))
        .route("live", r -> r.path("/api/live/**").filters(f -> f.stripPrefix(2)).uri(uris.liveUri()))
        .route("fundraising", r -> r.path("/api/fundraising/**").filters(f -> f.stripPrefix(2)).uri(uris.fundraisingUri()))
        .route("elections", r -> r.path("/api/elections/**").filters(f -> f.stripPrefix(2)).uri(uris.electionsUri()))
        .route("participation", r -> r.path("/api/participation/**").filters(f -> f.stripPrefix(2)).uri(uris.participationUri()))
        .route("messaging", r -> r.path("/api/messaging/**").filters(f -> f.stripPrefix(2)).uri(uris.messagingUri()))
        // notification-service's own controller is mapped at /notifications/** (plural), which
        // already equals the external namespace segment — same collision class as analytics and
        // assistant below, so only "/api" is stripped here (stripPrefix(1)), not stripPrefix(2).
        .route("notification", r -> r.path("/api/notifications/**").filters(f -> f.stripPrefix(1)).uri(uris.notificationUri()))
        .route("privacy", r -> r.path("/api/privacy/**").filters(f -> f.stripPrefix(2)).uri(uris.privacyUri()))
        // --- Phase 1 additions: legislative-service, activity-feed-service, analytics-service, assistant-service ---
        .route("legislative", r -> r.path("/api/legislative/**").filters(f -> f.stripPrefix(2)).uri(uris.legislativeUri()))
        .route("activity-feed", r -> r.path("/api/activity-feed/**").filters(f -> f.stripPrefix(2)).uri(uris.activityFeedUri()))
        // analytics-service/assistant-service's own controllers are mapped at /analytics/** and
        // /assistant/** respectively (the plan's intended resource naming) — which already equals
        // the external namespace segment, so only "/api" is stripped here (stripPrefix(1)), unlike
        // every other route above where the namespace segment must also be stripped.
        .route("analytics", r -> r.path("/api/analytics/**").filters(f -> f.stripPrefix(1)).uri(uris.analyticsUri()))
        .route("assistant", r -> r.path("/api/assistant/**").filters(f -> f.stripPrefix(1)).uri(uris.assistantUri()))
        .build();
  }
}
