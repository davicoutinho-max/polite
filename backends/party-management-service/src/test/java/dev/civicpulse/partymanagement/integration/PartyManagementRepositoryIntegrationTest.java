package dev.civicpulse.partymanagement.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import dev.civicpulse.partymanagement.application.port.out.AffiliationRequestRepository;
import dev.civicpulse.partymanagement.application.port.out.PartyProfileRepository;
import dev.civicpulse.partymanagement.application.port.out.PartyRepresentativeRepository;
import dev.civicpulse.partymanagement.domain.model.AffiliationRequest;
import dev.civicpulse.partymanagement.domain.model.PartyProfile;
import dev.civicpulse.partymanagement.domain.model.PartyRepresentative;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** Exercises the real JPA/Postgres adapters against the shared local-dev database (see
 * identity-service's equivalent test for the rationale on why this isn't Testcontainers). */
@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:postgresql://localhost:5432/party_management_service",
      "spring.datasource.username=party_management_service_app",
      "spring.datasource.password=party_mgmt_dev_pw"
    })
class PartyManagementRepositoryIntegrationTest {

  @BeforeAll
  static void requireLocalPostgres() {
    boolean reachable;
    try (Socket socket = new Socket()) {
      socket.connect(new InetSocketAddress("localhost", 5432), 500);
      reachable = true;
    } catch (Exception e) {
      reachable = false;
    }
    assumeTrue(reachable, "Shared dev Postgres (localhost:5432) is not running — start it with "
        + "'docker compose up -d postgres' in backends/ to run this test");
  }

  @Autowired private PartyProfileRepository profileRepository;
  @Autowired private PartyRepresentativeRepository representativeRepository;
  @Autowired private AffiliationRequestRepository requestRepository;

  // Every test below writes through the real JPA adapters into the shared local-dev Postgres —
  // the same database the live party-management-service instance serves the real app from.
  // Random-UUID rows left behind here are exactly the kind of dangling reference (pointing at a
  // politician/citizen account that was never actually created in identity-service) that later
  // surfaces as a 404 in the real frontend when it tries to resolve that person's profile. Every
  // test now deletes what it created.

  @Test
  void savesAndRetrievesPartyProfile() {
    UUID partyId = UUID.randomUUID();
    profileRepository.save(PartyProfile.createBlank(partyId, Instant.now()));

    try {
      assertThat(profileRepository.findByPartyId(partyId)).isPresent().get().satisfies(profile -> assertThat(profile.history()).isEmpty());
    } finally {
      profileRepository.deleteByPartyId(partyId);
    }
  }

  @Test
  void savesAndRetrievesRepresentative() {
    UUID partyId = UUID.randomUUID();
    UUID politicianId = UUID.randomUUID();
    UUID representativeId = UUID.randomUUID();
    representativeRepository.save(PartyRepresentative.link(representativeId, partyId, politicianId, "Deputy", Instant.now()));

    try {
      assertThat(representativeRepository.existsByPartyIdAndPoliticianAccountId(partyId, politicianId)).isTrue();
      assertThat(representativeRepository.findByPartyIdAndPoliticianAccountId(partyId, politicianId))
          .isPresent()
          .get()
          .satisfies(rep -> assertThat(rep.roleTitle()).contains("Deputy"));
    } finally {
      representativeRepository.delete(representativeId);
    }
  }

  @Test
  void savesAndApprovesAffiliationRequest() {
    UUID requestId = UUID.randomUUID();
    UUID partyId = UUID.randomUUID();
    UUID citizenId = UUID.randomUUID();
    requestRepository.save(AffiliationRequest.create(requestId, partyId, citizenId, "São Paulo", Instant.now()));

    try {
      AffiliationRequest found = requestRepository.findById(requestId).orElseThrow();
      found.approve(Instant.now());
      requestRepository.save(found);

      assertThat(requestRepository.findById(requestId)).isPresent().get().satisfies(req -> assertThat(req.decidedAt()).isPresent());
    } finally {
      requestRepository.deleteById(requestId);
    }
  }
}
