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

    try {
      assertThat(dossierRepository.existsById(politicianAccountId)).isTrue();
      assertThat(dossierRepository.findById(politicianAccountId)).isPresent().get().satisfies(d -> assertThat(d.speechesCount()).isZero());
    } finally {
      dossierRepository.deleteById(politicianAccountId);
    }
  }

  // These rows land in the shared local-dev Postgres, the same database the real
  // legislative-service instance serves `GET /legislative-items?recent=true` from — leaving them
  // behind means every test run injects another fake "PL 99/2026"/"Title" row that the real
  // frontend's Relevant Bills widget then shows to the user. Delete in FK order (item, then the
  // dossier stub it points at).
  @Test
  void savesAndRetrievesLegislativeItemWithCosponsors() {
    UUID politicianAccountId = UUID.randomUUID();
    dossierRepository.save(PoliticianDossierExtension.createStub(politicianAccountId, Instant.now()));
    UUID cosponsor = UUID.randomUUID();

    LegislativeItem saved =
        itemRepository.save(
            LegislativeItem.file(
                politicianAccountId, "PL 99/2026", "Title", "Summary", LegislativeItemCategory.PROJECT, LocalDate.now(), Set.of(cosponsor), Instant.now()));

    try {
      LegislativeItem found = itemRepository.findById(saved.id().orElseThrow()).orElseThrow();
      assertThat(found.cosponsorAccountIds()).containsExactly(cosponsor);
    } finally {
      itemRepository.deleteById(saved.id().orElseThrow());
      dossierRepository.deleteById(politicianAccountId);
    }
  }

  @Test
  void savesAndRetrievesAttendanceRecord() {
    UUID politicianAccountId = UUID.randomUUID();
    dossierRepository.save(PoliticianDossierExtension.createStub(politicianAccountId, Instant.now()));

    AttendanceRecord record = AttendanceRecord.initialize(politicianAccountId, Instant.now());
    record.recordPresence(true, Instant.now());
    attendanceRepository.save(record);

    try {
      assertThat(attendanceRepository.findById(politicianAccountId)).isPresent().get().satisfies(r -> assertThat(r.present()).isEqualTo(1));
    } finally {
      attendanceRepository.deleteById(politicianAccountId);
      dossierRepository.deleteById(politicianAccountId);
    }
  }
}
