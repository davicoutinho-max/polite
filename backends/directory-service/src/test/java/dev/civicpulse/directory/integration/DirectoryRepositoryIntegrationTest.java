package dev.civicpulse.directory.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import dev.civicpulse.directory.application.port.out.FollowRepository;
import dev.civicpulse.directory.application.port.out.PartyRepository;
import dev.civicpulse.directory.application.port.out.PoliticianRepository;
import dev.civicpulse.directory.domain.model.Follow;
import dev.civicpulse.directory.domain.model.FollowTargetType;
import dev.civicpulse.directory.domain.model.Party;
import dev.civicpulse.directory.domain.model.PartySpectrum;
import dev.civicpulse.directory.domain.model.Politician;
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
      "spring.datasource.url=jdbc:postgresql://localhost:5432/directory_service",
      "spring.datasource.username=directory_service_app",
      "spring.datasource.password=directory_dev_pw"
    })
class DirectoryRepositoryIntegrationTest {

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

  @Autowired private PoliticianRepository politicianRepository;
  @Autowired private PartyRepository partyRepository;
  @Autowired private FollowRepository followRepository;

  @Test
  void savesAndRetrievesPolitician() {
    Instant now = Instant.now();
    UUID accountId = UUID.randomUUID();
    Politician politician =
        Politician.project(accountId, "Jane Doe", "janedoe-" + System.nanoTime(), null, null, null, null, null, null, now);

    politicianRepository.save(politician);

    assertThat(politicianRepository.findById(accountId)).isPresent().get().satisfies(found -> {
      assertThat(found.name()).isEqualTo("Jane Doe");
      assertThat(found.followersCount()).isZero();
    });
  }

  @Test
  void savesAndRetrievesParty() {
    Instant now = Instant.now();
    UUID partyId = UUID.randomUUID();
    Party party =
        Party.project(partyId, "Progressive Party", "PROG" + System.nanoTime(), (int) (System.nanoTime() % 100000), "Progressivism", PartySpectrum.CENTER_LEFT, 1990, "Jane Doe", null, now);

    partyRepository.save(party);

    assertThat(partyRepository.findById(partyId)).isPresent().get().satisfies(found -> assertThat(found.acronym()).isEqualTo(party.acronym()));
  }

  @Test
  void followLifecyclePersistsAndDeletes() {
    Instant now = Instant.now();
    UUID follower = UUID.randomUUID();
    UUID targetId = UUID.randomUUID();

    assertThat(followRepository.exists(follower, FollowTargetType.POLITICIAN, targetId)).isFalse();

    followRepository.save(Follow.create(follower, FollowTargetType.POLITICIAN, targetId, now));
    assertThat(followRepository.exists(follower, FollowTargetType.POLITICIAN, targetId)).isTrue();
    assertThat(followRepository.findByFollower(follower)).hasSize(1);

    followRepository.delete(follower, FollowTargetType.POLITICIAN, targetId);
    assertThat(followRepository.exists(follower, FollowTargetType.POLITICIAN, targetId)).isFalse();
  }
}
