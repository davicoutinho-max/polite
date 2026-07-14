package dev.civicpulse.membershipaffiliation.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import dev.civicpulse.membershipaffiliation.application.port.out.AffiliationRepository;
import dev.civicpulse.membershipaffiliation.application.port.out.MembershipFeeRepository;
import dev.civicpulse.membershipaffiliation.domain.model.Affiliation;
import dev.civicpulse.membershipaffiliation.domain.model.MembershipFee;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** Exercises the real JPA/Postgres adapters against the shared local-dev database (see
 * identity-service's equivalent test for the rationale on why this isn't Testcontainers). */
@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:postgresql://localhost:5432/membership_affiliation_service",
      "spring.datasource.username=membership_affiliation_service_app",
      "spring.datasource.password=membership_dev_pw"
    })
class MembershipAffiliationRepositoryIntegrationTest {

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

  @Autowired private AffiliationRepository affiliationRepository;
  @Autowired private MembershipFeeRepository feeRepository;

  @Test
  void savesAndRetrievesAffiliationThroughAStatusTransition() {
    UUID id = UUID.randomUUID();
    UUID citizenId = UUID.randomUUID();
    UUID partyId = UUID.randomUUID();
    Affiliation affiliation = Affiliation.request(id, citizenId, partyId, Instant.now());
    affiliationRepository.save(affiliation);

    assertThat(affiliationRepository.existsActiveByCitizenAndParty(citizenId, partyId)).isTrue();

    Affiliation found = affiliationRepository.findById(id).orElseThrow();
    found.startReview(Instant.now());
    affiliationRepository.save(found);

    Affiliation reloaded = affiliationRepository.findById(id).orElseThrow();
    assertThat(reloaded.status().code()).isEqualTo("under_review");
  }

  @Test
  void rejectedAffiliationDoesNotCountAsActive() {
    UUID id = UUID.randomUUID();
    UUID citizenId = UUID.randomUUID();
    UUID partyId = UUID.randomUUID();
    Affiliation affiliation = Affiliation.request(id, citizenId, partyId, Instant.now());
    affiliation.reject(Instant.now());
    affiliationRepository.save(affiliation);

    assertThat(affiliationRepository.existsActiveByCitizenAndParty(citizenId, partyId)).isFalse();
  }

  @Test
  void savesAndRetrievesMembershipFee() {
    UUID affiliationId = UUID.randomUUID();
    affiliationRepository.save(Affiliation.request(affiliationId, UUID.randomUUID(), UUID.randomUUID(), Instant.now()));

    UUID feeId = UUID.randomUUID();
    MembershipFee fee = MembershipFee.generate(feeId, affiliationId, "2026-99-" + System.nanoTime(), 5000, LocalDate.now().plusDays(30));

    feeRepository.save(fee);

    assertThat(feeRepository.findById(feeId)).isPresent().get().satisfies(found -> assertThat(found.amountCents()).isEqualTo(5000));
  }
}
