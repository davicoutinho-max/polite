package dev.civicpulse.privacycompliance.domain.model;

import java.util.Set;

/** The fixed set of microservices expected to report an erasure back into
 * {@code erasure_audit_log} for every account-deletion saga — i.e. every service in this system
 * that stores its own copy of account-scoped personal data. Deliberately excludes services with
 * no per-account PII of their own: elections-service (public calendar data only) and
 * platform-configuration-service (global, not account-scoped), plus this service itself (it
 * orchestrates the saga, it doesn't participate in it). */
public final class ExpectedErasureServices {

  public static final Set<String> ALL =
      Set.of(
          "identity-service",
          "directory-service",
          "party-management-service",
          "membership-affiliation-service",
          "payments-service",
          "feed-content-service",
          "live-streaming-service",
          "fundraising-service",
          "participation-service",
          "messaging-service",
          "notification-service");

  private ExpectedErasureServices() {}
}
