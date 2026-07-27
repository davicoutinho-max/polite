package dev.civicpulse.elections.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.civicpulse.elections.application.port.in.ManageElectionUseCase;
import dev.civicpulse.elections.application.port.out.ElectionCandidacyRepository;
import dev.civicpulse.elections.application.port.out.ElectionRepository;
import dev.civicpulse.elections.application.port.out.ElectionResultRepository;
import dev.civicpulse.elections.domain.exception.ElectionNotFoundException;
import dev.civicpulse.elections.domain.model.Election;
import dev.civicpulse.elections.domain.model.ElectionScope;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ElectionServiceTest {

  @Mock private ElectionRepository electionRepository;
  @Mock private ElectionCandidacyRepository electionCandidacyRepository;
  @Mock private ElectionResultRepository electionResultRepository;

  private ElectionService service;

  @BeforeEach
  void setUp() {
    service = new ElectionService(electionRepository, electionCandidacyRepository, electionResultRepository);
  }

  @Test
  void createSavesElection() {
    when(electionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    Election election = service.create("Eleicoes 2026", ElectionScope.NACIONAL, LocalDate.of(2026, 10, 4), "desc");

    assertThat(election.title()).isEqualTo("Eleicoes 2026");
  }

  @Test
  void syncElectionCreatesWhenNoMatchExists() {
    when(electionRepository.findByScopeAndElectionDateAndLocation(ElectionScope.ESTADUAL, LocalDate.of(2022, 10, 2), "SE"))
        .thenReturn(Optional.empty());
    when(electionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    Election election = service.syncElection("Eleições Estaduais 2022 — SE", ElectionScope.ESTADUAL, LocalDate.of(2022, 10, 2), "SE", null);

    assertThat(election.title()).isEqualTo("Eleições Estaduais 2022 — SE");
    assertThat(election.location()).contains("SE");
  }

  @Test
  void syncElectionReturnsExistingWithoutSavingAgain() {
    Election existing = Election.create(UUID.randomUUID(), "Eleições Estaduais 2022 — SE", ElectionScope.ESTADUAL, LocalDate.of(2022, 10, 2), "SE", null);
    when(electionRepository.findByScopeAndElectionDateAndLocation(ElectionScope.ESTADUAL, LocalDate.of(2022, 10, 2), "SE"))
        .thenReturn(Optional.of(existing));

    Election result = service.syncElection("Eleições Estaduais 2022 — SE", ElectionScope.ESTADUAL, LocalDate.of(2022, 10, 2), "SE", null);

    assertThat(result).isSameAs(existing);
    verify(electionRepository, never()).save(any());
  }

  @Test
  void nominateCandidateThrowsWhenElectionMissing() {
    UUID electionId = UUID.randomUUID();
    when(electionRepository.findById(electionId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.nominateCandidate(electionId, UUID.randomUUID())).isInstanceOf(ElectionNotFoundException.class);
  }

  @Test
  void nominateCandidateIsIdempotent() {
    UUID electionId = UUID.randomUUID();
    UUID politicianId = UUID.randomUUID();
    Election election = Election.create(electionId, "title", ElectionScope.NACIONAL, LocalDate.now(), null, null);
    when(electionRepository.findById(electionId)).thenReturn(Optional.of(election));
    when(electionCandidacyRepository.exists(electionId, politicianId)).thenReturn(true);

    service.nominateCandidate(electionId, politicianId);

    verify(electionCandidacyRepository, never()).save(any());
  }

  @Test
  void nominateCandidateSavesWhenNotAlreadyNominated() {
    UUID electionId = UUID.randomUUID();
    UUID politicianId = UUID.randomUUID();
    Election election = Election.create(electionId, "title", ElectionScope.NACIONAL, LocalDate.now(), null, null);
    when(electionRepository.findById(electionId)).thenReturn(Optional.of(election));
    when(electionCandidacyRepository.exists(electionId, politicianId)).thenReturn(false);

    service.nominateCandidate(electionId, politicianId);

    verify(electionCandidacyRepository).save(any());
  }

  @Test
  void syncElectionResultsThrowsWhenElectionMissing() {
    UUID electionId = UUID.randomUUID();
    when(electionRepository.findById(electionId)).thenReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                service.syncElectionResults(
                    electionId, "Governador", List.of(new ManageElectionUseCase.ResultInput("1", "Jane Doe", "PRO", 1000L, 1, true, null))))
        .isInstanceOf(ElectionNotFoundException.class);
  }

  @Test
  void syncElectionResultsReplacesResultsForOffice() {
    UUID electionId = UUID.randomUUID();
    Election election = Election.create(electionId, "title", ElectionScope.ESTADUAL, LocalDate.now(), "SE", null);
    when(electionRepository.findById(electionId)).thenReturn(Optional.of(election));
    List<ManageElectionUseCase.ResultInput> inputs =
        List.of(
            new ManageElectionUseCase.ResultInput("1", "Jane Doe", "PRO", 5000L, 1, true, UUID.randomUUID()),
            new ManageElectionUseCase.ResultInput("2", "John Roe", "CON", 3000L, 2, false, null));

    service.syncElectionResults(electionId, "Governador", inputs);

    verify(electionResultRepository)
        .replaceForOffice(
            org.mockito.ArgumentMatchers.eq(electionId),
            org.mockito.ArgumentMatchers.eq("Governador"),
            org.mockito.ArgumentMatchers.argThat(results -> results.size() == 2));
  }
}
