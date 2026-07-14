package dev.civicpulse.legislative.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import dev.civicpulse.legislative.application.port.out.AttendanceRecordRepository;
import dev.civicpulse.legislative.application.port.out.LegislativeItemRepository;
import dev.civicpulse.legislative.application.port.out.PoliticianDossierRepository;
import dev.civicpulse.legislative.domain.model.AttendanceRecord;
import dev.civicpulse.legislative.domain.model.LegislativeItem;
import dev.civicpulse.legislative.domain.model.LegislativeItemCategory;
import dev.civicpulse.legislative.domain.model.PoliticianDossierExtension;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** Exercises the real JPA/Postgres adapters against the shared local-dev database (see
 * identity-service's equivalent test for the rationale on why this isn't Testcontainers). */
@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:postgresql://localhost:5432/legislative_service",
      "spring.datasource.username=legislative_service_app",
      "spring.datasource.password=legislative_dev_pw"
    })
class LegislativeRepositoryIntegrationTest {

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

  @Autowired private PoliticianDossierRepository dossierRepository;
  @Autowired private LegislativeItemRepository itemRepository;
  @Autowired private AttendanceRecordRepository attendanceRepository;

  @Test
  void savesAndRetrievesDossierStub() {
    UUID politicianAccountId = UUID.randomUUID();
    dossierRepository.save(PoliticianDossierExtension.createStub(politicianAccountId, Instant.now()));

    assertThat(dossierRepository.existsById(politicianAccountId)).isTrue();
    assertThat(dossierRepository.findById(politicianAccountId)).isPresent().get().satisfies(d -> assertThat(d.speechesCount()).isZero());
  }

  @Test
  void savesAndRetrievesLegislativeItemWithCosponsors() {
    UUID politicianAccountId = UUID.randomUUID();
    dossierRepository.save(PoliticianDossierExtension.createStub(politicianAccountId, Instant.now()));
    UUID cosponsor = UUID.randomUUID();

    LegislativeItem saved =
        itemRepository.save(
            LegislativeItem.file(
                politicianAccountId, "PL 99/2026", "Title", "Summary", LegislativeItemCategory.PROJECT, LocalDate.now(), Set.of(cosponsor), Instant.now()));

    LegislativeItem found = itemRepository.findById(saved.id().orElseThrow()).orElseThrow();
    assertThat(found.cosponsorAccountIds()).containsExactly(cosponsor);
  }

  @Test
  void savesAndRetrievesAttendanceRecord() {
    UUID politicianAccountId = UUID.randomUUID();
    dossierRepository.save(PoliticianDossierExtension.createStub(politicianAccountId, Instant.now()));

    AttendanceRecord record = AttendanceRecord.initialize(politicianAccountId, Instant.now());
    record.recordPresence(true, Instant.now());
    attendanceRepository.save(record);

    assertThat(attendanceRepository.findById(politicianAccountId)).isPresent().get().satisfies(r -> assertThat(r.present()).isEqualTo(1));
  }
}
