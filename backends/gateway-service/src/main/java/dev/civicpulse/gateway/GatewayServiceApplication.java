package dev.civicpulse.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Single ingress for every CivicPulse backend (see docs/architecture/system-architecture.html):
 * validates the JWT identity-service mints, forwards a trusted {@code X-Account-Id} (+ {@code
 * X-Account-Type}/{@code X-Account-Permissions}) downstream, resolves browser CORS, and routes
 * by a {@code /api/<service>/**} namespace (see RouteConfig's javadoc for why namespacing, not
 * raw path proxying, was necessary). Unlike the 14 domain services, this one has no database and
 * no domain/application layers — it's pure routing + a security filter.
 *
 * <p>Deliberately deferred in this pass (see the integration plan): Redis-backed per-route rate
 * limiting, and per-service Spring Security/permission re-enforcement downstream (today every
 * backend simply trusts {@code X-Account-Id} once it's present — this gateway is the only place
 * that currently checks anything at all).
 */
@SpringBootApplication
public class GatewayServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(GatewayServiceApplication.class, args);
  }
}
