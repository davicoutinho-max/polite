package dev.civicpulse.platformconfig.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import dev.civicpulse.platformconfig.application.port.out.CountryRepository;
import dev.civicpulse.platformconfig.application.port.out.LanguageRepository;
import dev.civicpulse.platformconfig.application.port.out.PartyRegistryRepository;
import dev.civicpulse.platformconfig.application.port.out.StateRepository;
import dev.civicpulse.platformconfig.domain.model.Country;
import dev.civicpulse.platformconfig.domain.model.Language;
import dev.civicpulse.platformconfig.domain.model.PartyRegistryEntry;
import dev.civicpulse.platformconfig.domain.model.State;
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
      "spring.datasource.url=jdbc:postgresql://localhost:5432/platform_configuration_service",
      "spring.datasource.username=platform_configuration_service_app",
      "spring.datasource.password=platform_cfg_dev_pw"
    })
class PlatformConfigRepositoryIntegrationTest {

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

  @Autowired private PartyRegistryRepository partyRegistryRepository;
  @Autowired private CountryRepository countryRepository;
  @Autowired private LanguageRepository languageRepository;
  @Autowired private StateRepository stateRepository;

  @Test
  void savesAndRetrievesPartyRegistryEntry() {
    UUID id = UUID.randomUUID();
    int uniqueNumber = (int) (System.nanoTime() % 1_000_000);
    PartyRegistryEntry entry =
        PartyRegistryEntry.register(id, "Progressive Party", "PROG" + System.nanoTime(), uniqueNumber, "Jane Doe", "Progressivism", Instant.now());

    partyRegistryRepository.save(entry);

    assertThat(partyRegistryRepository.findById(id)).isPresent().get().satisfies(found -> assertThat(found.name()).isEqualTo("Progressive Party"));
    assertThat(partyRegistryRepository.existsByAcronym(entry.acronym())).isTrue();
  }

  @Test
  void savesAndDeletesCountry() {
    UUID id = UUID.randomUUID();
    String code = "Z" + (char) ('A' + (System.nanoTime() % 26));
    countryRepository.save(Country.create(id, "Test Country", code));

    assertThat(countryRepository.findById(id)).isPresent();

    countryRepository.delete(id);

    assertThat(countryRepository.findById(id)).isEmpty();
  }

  @Test
  void savesFindsAndDeletesState() {
    UUID countryId = UUID.randomUUID();
    countryRepository.save(Country.create(countryId, "Test Country For States", "Y" + (char) ('A' + (System.nanoTime() % 26))));
    UUID stateId = UUID.randomUUID();
    stateRepository.save(State.create(stateId, countryId, "Test State", "TS"));

    assertThat(stateRepository.findByCountryId(countryId)).anySatisfy(s -> assertThat(s.id()).isEqualTo(stateId));

    stateRepository.delete(stateId);

    assertThat(stateRepository.findByCountryId(countryId)).noneSatisfy(s -> assertThat(s.id()).isEqualTo(stateId));
  }

  @Test
  void languageDefaultFlagInvariantHoldsAcrossTwoRows() {
    String firstId = "test-lang-a-" + System.nanoTime();
    String secondId = "test-lang-b-" + System.nanoTime();
    // Clear any pre-existing default first — the partial unique index allows at most one
    // is_default=true row across the whole table, not just this test's rows.
    languageRepository.clearDefaultFlag();
    languageRepository.save(Language.create(firstId, "Test Language A", "ta", true));
    languageRepository.save(Language.create(secondId, "Test Language B", "tb", false));

    assertThat(languageRepository.findById(firstId)).isPresent().get().satisfies(l -> assertThat(l.isDefault()).isTrue());

    languageRepository.clearDefaultFlag();
    languageRepository.markAsDefault(secondId);

    assertThat(languageRepository.findById(firstId)).isPresent().get().satisfies(l -> assertThat(l.isDefault()).isFalse());
    assertThat(languageRepository.findById(secondId)).isPresent().get().satisfies(l -> assertThat(l.isDefault()).isTrue());
  }
}
