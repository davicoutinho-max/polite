package dev.civicpulse.elections.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.civicpulse.elections.application.port.out.ElectionCandidacyRepository;
import dev.civicpulse.elections.application.port.out.ElectionRepository;
import dev.civicpulse.elections.application.port.out.PoliticianDirectoryGateway;
import dev.civicpulse.elections.application.port.out.PoliticianDirectoryGateway.PoliticianSummary;
import dev.civicpulse.elections.domain.exception.ElectionNotFoundException;
import dev.civicpulse.elections.domain.model.Election;
import dev.civicpulse.elections.domain.model.ElectionCandidacy;
import dev.civicpulse.elections.domain.model.ElectionScope;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ElectionQueryServiceTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Mock private ElectionRepository electionRepository;
  @Mock private ElectionCandidacyRepository electionCandidacyRepository;
  @Mock private PoliticianDirectoryGateway politicianDirectoryGateway;

  private ElectionQueryService service;

  @BeforeEach
  void setUp() {
    service =
        new ElectionQueryService(electionRepository, electionCandidacyRepository, politicianDirectoryGateway, Clock.fixed(NOW, ZoneOffset.UTC));
  }

  @Test
  void getByIdThrowsWhenMissing() {
    UUID id = UUID.randomUUID();
    when(electionRepository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.getById(id)).isInstanceOf(ElectionNotFoundException.class);
  }

  @Test
  void listWithNullScopeDelegatesToFindAll() {
    service.list(null, 0, 20);

    verify(electionRepository).findAll(0, 20);
  }

  @Test
  void listWithScopeDelegatesToFindByScope() {
    service.list(ElectionScope.ESTADUAL, 1, 10);

    verify(electionRepository).findByScope(ElectionScope.ESTADUAL, 1, 10);
  }

  @Test
  void listUpcomingUsesClockDate() {
    service.listUpcoming(0, 20);

    verify(electionRepository).findUpcoming(LocalDate.now(Clock.fixed(NOW, ZoneOffset.UTC)), 0, 20);
  }

  @Test
  void listCandidatesThrowsWhenElectionMissing() {
    UUID electionId = UUID.randomUUID();
    when(electionRepository.findById(electionId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.listCandidates(electionId)).isInstanceOf(ElectionNotFoundException.class);
  }

  @Test
  void listCandidatesOmitsUnresolvablePoliticians() {
    UUID electionId = UUID.randomUUID();
    UUID resolvableId = UUID.randomUUID();
    UUID goneId = UUID.randomUUID();
    Election election = Election.create(electionId, "title", ElectionScope.NACIONAL, LocalDate.now(), null);
    when(electionRepository.findById(electionId)).thenReturn(Optional.of(election));
    when(electionCandidacyRepository.findByElectionId(electionId))
        .thenReturn(List.of(ElectionCandidacy.nominate(electionId, resolvableId), ElectionCandidacy.nominate(electionId, goneId)));
    when(politicianDirectoryGateway.lookup(resolvableId))
        .thenReturn(Optional.of(new PoliticianSummary(resolvableId, "Jane Doe", "jane", null, true, "Deputy", "PRO")));
    when(politicianDirectoryGateway.lookup(goneId)).thenReturn(Optional.empty());

    List<PoliticianSummary> candidates = service.listCandidates(electionId);

    assertThat(candidates).hasSize(1);
    assertThat(candidates.get(0).accountId()).isEqualTo(resolvableId);
  }
}
