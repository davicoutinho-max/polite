package dev.civicpulse.elections;

import dev.civicpulse.elections.adapter.out.client.DirectoryServiceProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Scope note: schema.sql publishes and consumes no domain events for this service — the election
 * calendar is a small, mostly-static, visitor-facing read surface with no cross-service reactions
 * to model, so there is deliberately no Kafka producer or consumer here (unlike every other
 * backend in this system). Candidate profile details are resolved from directory-service at query
 * time via a real synchronous REST call (see PoliticianDirectoryGateway) rather than replicated
 * locally, per {@code election_candidacies}' comment in schema.sql.
 */
@SpringBootApplication
@EnableConfigurationProperties(DirectoryServiceProperties.class)
public class ElectionsServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(ElectionsServiceApplication.class, args);
  }
}
