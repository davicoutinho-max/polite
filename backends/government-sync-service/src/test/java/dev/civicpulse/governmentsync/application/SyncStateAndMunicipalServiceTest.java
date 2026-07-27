package dev.civicpulse.governmentsync.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.civicpulse.governmentsync.application.port.in.SyncStateAndMunicipalUseCase.SyncResult;
import dev.civicpulse.governmentsync.application.port.out.ElectionSyncGateway;
import dev.civicpulse.governmentsync.application.port.out.PartySyncGateway;
import dev.civicpulse.governmentsync.application.port.out.PoliticianSyncGateway;
import dev.civicpulse.governmentsync.application.port.out.PoliticianSyncGateway.SyncPoliticianCommand;
import dev.civicpulse.governmentsync.application.port.out.TseElectionDataGateway;
import dev.civicpulse.governmentsync.application.port.out.TseElectionDataGateway.TseElectedCandidate;
import dev.civicpulse.governmentsync.application.port.out.TseElectionDataGateway.TseElectionResult;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SyncStateAndMunicipalServiceTest {

  @Mock private TseElectionDataGateway tseElectionDataGateway;
  @Mock private PartySyncGateway partySyncGateway;
  @Mock private PoliticianSyncGateway politicianSyncGateway;
  @Mock private ElectionSyncGateway electionSyncGateway;

  private SyncStateAndMunicipalService service;

  @BeforeEach
  void setUp() {
    service = new SyncStateAndMunicipalService(tseElectionDataGateway, partySyncGateway, politicianSyncGateway, electionSyncGateway);
    when(tseElectionDataGateway.fetchElectionResults(ArgumentMatchers.anyInt(), any(), ArgumentMatchers.anySet(), ArgumentMatchers.anyBoolean()))
        .thenReturn(List.of());
  }

  @Test
  void syncsStateAndMunicipalCandidatesWithCorrectGovLevelAndLocation() {
    UUID psdId = UUID.randomUUID();
    when(tseElectionDataGateway.fetchElectedCandidates(eq(2022), eq("SE"), ArgumentMatchers.anySet()))
        .thenReturn(
            List.of(
                new TseElectedCandidate(
                    "260001636411", "JEFERSON LUIZ DE ANDRADE", "JEFERSON ANDRADE", "Deputado Estadual", "PSD", 55, "Partido Social Democrático", "SE", "CANINDÉ DE SÃO FRANCISCO")));
    when(tseElectionDataGateway.fetchElectedCandidates(eq(2024), eq("SE"), ArgumentMatchers.anySet()))
        .thenReturn(
            List.of(
                new TseElectedCandidate(
                    "260002070692", "JOALDO FELICIO DE ANDRADE", "JOALDO", "Vereador", "PP", 11, "Progressistas", "SE", "SANTA LUZIA DO ITANHY")));
    when(partySyncGateway.syncParty(any())).thenReturn(psdId, UUID.randomUUID());

    SyncResult result = service.syncStateAndMunicipal("SE");

    assertThat(result.stateSynced()).isEqualTo(1);
    assertThat(result.municipalSynced()).isEqualTo(1);
    assertThat(result.partiesSynced()).isEqualTo(2);
    assertThat(result.failures()).isZero();

    verify(politicianSyncGateway)
        .syncPolitician(
            any(),
            ArgumentMatchers.argThat(
                (SyncPoliticianCommand c) ->
                    "state".equals(c.govLevel()) && "SE".equals(c.state()) && "TSE_ESTADUAL".equals(c.externalSource()) && c.name().equals("Jeferson Andrade")));
    verify(politicianSyncGateway)
        .syncPolitician(
            any(),
            ArgumentMatchers.argThat(
                (SyncPoliticianCommand c) ->
                    "municipal".equals(c.govLevel())
                        && "SANTA LUZIA DO ITANHY".equals(c.state())
                        && "TSE_MUNICIPAL".equals(c.externalSource())));
  }

  @Test
  void candidatesSharingAPartyOnlySyncThatPartyOnce() {
    when(tseElectionDataGateway.fetchElectedCandidates(eq(2022), eq("SE"), ArgumentMatchers.anySet()))
        .thenReturn(
            List.of(
                new TseElectedCandidate("1", "A", "A", "Deputado Estadual", "PSD", 55, "Partido Social Democrático", "SE", "M1"),
                new TseElectedCandidate("2", "B", "B", "Deputado Estadual", "PSD", 55, "Partido Social Democrático", "SE", "M2")));
    when(tseElectionDataGateway.fetchElectedCandidates(eq(2024), eq("SE"), ArgumentMatchers.anySet())).thenReturn(List.of());
    when(partySyncGateway.syncParty(any())).thenReturn(UUID.randomUUID());

    SyncResult result = service.syncStateAndMunicipal("SE");

    assertThat(result.partiesSynced()).isEqualTo(1);
    verify(partySyncGateway, times(1)).syncParty(any());
    verify(politicianSyncGateway, times(2)).syncPolitician(any(), any());
  }

  @Test
  void perCandidateFailureIsCountedNotFatal() {
    when(tseElectionDataGateway.fetchElectedCandidates(eq(2022), eq("SE"), ArgumentMatchers.anySet()))
        .thenReturn(List.of(new TseElectedCandidate("1", "A", "A", "Governador", "PSD", 55, "Partido Social Democrático", "SE", "SE")));
    when(tseElectionDataGateway.fetchElectedCandidates(eq(2024), eq("SE"), ArgumentMatchers.anySet())).thenReturn(List.of());
    when(partySyncGateway.syncParty(any())).thenThrow(new RuntimeException("boom"));

    SyncResult result = service.syncStateAndMunicipal("SE");

    assertThat(result.failures()).isEqualTo(1);
    assertThat(result.stateSynced()).isZero();
  }

  @Test
  void electionResultsAreGroupedByRaceAndPushedWithPoliticianIdForWinnersOnly() {
    UUID winnerId = UUID.randomUUID();
    when(tseElectionDataGateway.fetchElectedCandidates(eq(2022), eq("SE"), ArgumentMatchers.anySet()))
        .thenReturn(
            List.of(new TseElectedCandidate("1", "WINNER", "WINNER", "Governador", "PSD", 55, "Partido Social Democrático", "SE", "SE")));
    when(tseElectionDataGateway.fetchElectedCandidates(eq(2024), eq("SE"), ArgumentMatchers.anySet())).thenReturn(List.of());
    when(tseElectionDataGateway.fetchElectionResults(eq(2022), eq("SE"), ArgumentMatchers.anySet(), eq(false)))
        .thenReturn(
            List.of(
                new TseElectionResult("1", "WINNER", "WINNER", "Governador", "PSD", 55, "Partido Social Democrático", "SE", "SE", 5000L, 1, true),
                new TseElectionResult("2", "LOSER", "LOSER", "Governador", "PP", 11, "Progressistas", "SE", "SE", 3000L, 2, false)));
    when(partySyncGateway.syncParty(any())).thenReturn(winnerId);
    when(politicianSyncGateway.syncPolitician(any(), any())).thenReturn(winnerId);

    service.syncStateAndMunicipal("SE");

    verify(electionSyncGateway)
        .syncElectionResults(
            any(),
            eq("estadual"),
            any(),
            eq("SE"),
            eq("Governador"),
            ArgumentMatchers.argThat(
                candidates ->
                    candidates.size() == 2
                        && candidates.get(0).externalId().equals("1")
                        && candidates.get(0).politicianAccountId().equals(winnerId)
                        && candidates.get(1).externalId().equals("2")
                        && candidates.get(1).politicianAccountId() == null));
  }
}
