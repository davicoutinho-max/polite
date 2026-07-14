package dev.civicpulse.elections.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import dev.civicpulse.elections.application.port.out.ElectionCandidacyRepository;
import dev.civicpulse.elections.application.port.out.ElectionRepository;
import dev.civicpulse.elections.domain.model.Election;
import dev.civicpulse.elections.domain.model.ElectionCandidacy;
import dev.civicpulse.elections.domain.model.ElectionScope;
import java.net.InetSocketAddress;
import java.net.Socket;
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
      "spring.datasource.url=jdbc:postgresql://localhost:5432/elections_service",
      "spring.datasource.username=elections_service_app",
      "spring.datasource.password=elections_dev_pw"
    })
class ElectionsRepositoryIntegrationTest {

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

  @Autowired private ElectionRepository electionRepository;
  @Autowired private ElectionCandidacyRepository electionCandidacyRepository;

  @Test
  void savesAndRetrievesElection() {
    UUID id = UUID.randomUUID();
    Election election = Election.create(id, "Eleicoes Municipais 2026", ElectionScope.MUNICIPAL, LocalDate.of(2026, 10, 4), "desc");

    electionRepository.save(election);

    assertThat(electionRepository.findById(id)).isPresent().get().satisfies(found -> assertThat(found.title()).isEqualTo("Eleicoes Municipais 2026"));
  }

  @Test
  void findByScopeFiltersCorrectly() {
    UUID nacionalId = UUID.randomUUID();
    UUID estadualId = UUID.randomUUID();
    electionRepository.save(Election.create(nacionalId, "national one", ElectionScope.NACIONAL, LocalDate.now(), null));
    electionRepository.save(Election.create(estadualId, "state one", ElectionScope.ESTADUAL, LocalDate.now(), null));

    assertThat(electionRepository.findByScope(ElectionScope.ESTADUAL, 0, 50)).extracting(Election::id).contains(estadualId).doesNotContain(nacionalId);
  }

  @Test
  void findUpcomingExcludesPastElections() {
    UUID pastId = UUID.randomUUID();
    UUID futureId = UUID.randomUUID();
    electionRepository.save(Election.create(pastId, "past one", ElectionScope.NACIONAL, LocalDate.now().minusYears(5), null));
    electionRepository.save(Election.create(futureId, "future one", ElectionScope.NACIONAL, LocalDate.now().plusYears(1), null));

    assertThat(electionRepository.findUpcoming(LocalDate.now(), 0, 100)).extracting(Election::id).contains(futureId).doesNotContain(pastId);
  }

  @Test
  void candidacyRoundTripAndExistsCheck() {
    UUID electionId = UUID.randomUUID();
    UUID politicianId = UUID.randomUUID();
    electionRepository.save(Election.create(electionId, "title", ElectionScope.NACIONAL, LocalDate.now(), null));

    assertThat(electionCandidacyRepository.exists(electionId, politicianId)).isFalse();

    electionCandidacyRepository.save(ElectionCandidacy.nominate(electionId, politicianId));

    assertThat(electionCandidacyRepository.exists(electionId, politicianId)).isTrue();
    assertThat(electionCandidacyRepository.findByElectionId(electionId)).anySatisfy(c -> assertThat(c.politicianAccountId()).isEqualTo(politicianId));
  }
}
