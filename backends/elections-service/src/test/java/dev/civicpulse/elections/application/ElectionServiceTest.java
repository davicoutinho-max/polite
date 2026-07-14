package dev.civicpulse.elections.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.civicpulse.elections.application.port.out.ElectionCandidacyRepository;
import dev.civicpulse.elections.application.port.out.ElectionRepository;
import dev.civicpulse.elections.domain.exception.ElectionNotFoundException;
import dev.civicpulse.elections.domain.model.Election;
import dev.civicpulse.elections.domain.model.ElectionScope;
import java.time.LocalDate;
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

  private ElectionService service;

  @BeforeEach
  void setUp() {
    service = new ElectionService(electionRepository, electionCandidacyRepository);
  }

  @Test
  void createSavesElection() {
    when(electionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    Election election = service.create("Eleicoes 2026", ElectionScope.NACIONAL, LocalDate.of(2026, 10, 4), "desc");

    assertThat(election.title()).isEqualTo("Eleicoes 2026");
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
    Election election = Election.create(electionId, "title", ElectionScope.NACIONAL, LocalDate.now(), null);
    when(electionRepository.findById(electionId)).thenReturn(Optional.of(election));
    when(electionCandidacyRepository.exists(electionId, politicianId)).thenReturn(true);

    service.nominateCandidate(electionId, politicianId);

    verify(electionCandidacyRepository, never()).save(any());
  }

  @Test
  void nominateCandidateSavesWhenNotAlreadyNominated() {
    UUID electionId = UUID.randomUUID();
    UUID politicianId = UUID.randomUUID();
    Election election = Election.create(electionId, "title", ElectionScope.NACIONAL, LocalDate.now(), null);
    when(electionRepository.findById(electionId)).thenReturn(Optional.of(election));
    when(electionCandidacyRepository.exists(electionId, politicianId)).thenReturn(false);

    service.nominateCandidate(electionId, politicianId);

    verify(electionCandidacyRepository).save(any());
  }
}
