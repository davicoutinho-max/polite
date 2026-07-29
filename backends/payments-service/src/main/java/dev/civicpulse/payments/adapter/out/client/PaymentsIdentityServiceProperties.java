package dev.civicpulse.payments.adapter.out.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Identity-service is called directly here (not through the Gateway) — this is a
 * service-to-service call, and the endpoint it hits ({@code /accounts/{id}/payment-profile}) is
 * explicitly blocked at the Gateway anyway (see identity-service's RouteConfig). */
@ConfigurationProperties(prefix = "payments.identity-service")
public record PaymentsIdentityServiceProperties(String baseUrl) {}
