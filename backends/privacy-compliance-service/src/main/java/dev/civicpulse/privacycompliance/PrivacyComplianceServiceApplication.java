package dev.civicpulse.privacycompliance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Scope note: this service orchestrates every other service's account-scoped data but owns none
 * of it (see schema.sql's header). Two things are deliberately simulated rather than fully
 * automated in this pass: (1) data-export file generation — a real system would have a background
 * worker call {@code POST /data-export-requests/{id}/start-processing} then {@code .../ready}
 * once it has actually produced the export; here those are just REST transitions a caller (or an
 * ops script) drives directly. (2) the {@code <ServiceName>ErasureCompleted} family from
 * schema.sql's consumed-events list is implemented as one generic {@code ErasureCompletedMessage}
 * on a single shared {@code erasure-completed} topic, carrying {@code serviceName} as a field
 * rather than one Kafka topic per reporting service — a stable contract every current and future
 * service can report against without this service needing a new listener method each time one is
 * added. {@link dev.civicpulse.privacycompliance.domain.model.ExpectedErasureServices} is the
 * fixed roster of services expected to report before a deletion saga auto-completes.
 */
@SpringBootApplication
public class PrivacyComplianceServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(PrivacyComplianceServiceApplication.class, args);
  }
}
