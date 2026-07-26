package dev.civicpulse.governmentsync.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.civicpulse.governmentsync.application.port.in.SyncFederalLegislatureUseCase.SyncResult;
import dev.civicpulse.governmentsync.application.port.out.CamaraGateway;
import dev.civicpulse.governmentsync.application.port.out.CamaraGateway.CamaraDeputy;
import dev.civicpulse.governmentsync.application.port.out.CamaraGateway.CamaraParty;
import dev.civicpulse.governmentsync.application.port.out.LegislativeDossierGateway;
import dev.civicpulse.governmentsync.application.port.out.PartySyncGateway;
import dev.civicpulse.governmentsync.application.port.out.PartySyncGateway.SyncPartyCommand;
import dev.civicpulse.governmentsync.application.port.out.PoliticianSyncGateway;
import dev.civicpulse.governmentsync.application.port.out.PoliticianSyncGateway.SyncPoliticianCommand;
import dev.civicpulse.governmentsync.application.port.out.SenadoGateway;
import dev.civicpulse.governmentsync.application.port.out.SenadoGateway.SenadoSenator;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SyncFederalLegislatureServiceTest {

  @Mock private CamaraGateway camaraGateway;
  @Mock private SenadoGateway senadoGateway;
  @Mock private PartySyncGateway partySyncGateway;
  @Mock private PoliticianSyncGateway politicianSyncGateway;
  @Mock private LegislativeDossierGateway legislativeDossierGateway;

  private SyncFederalLegislatureService service;

  @BeforeEach
  void setUp() {
    service = new SyncFederalLegislatureService(camaraGateway, senadoGateway, partySyncGateway, politicianSyncGateway, legislativeDossierGateway);
  }

  @Test
  void syncsPartiesDeputiesAndSenatorsLinkingByAcronym() {
    UUID mdbId = UUID.randomUUID();
    when(camaraGateway.fetchAllParties()).thenReturn(List.of(new CamaraParty("36899", "MDB", "Movimento Democrático Brasileiro", "http://logo", null)));
    when(partySyncGateway.syncParty(any())).thenReturn(mdbId);
    when(camaraGateway.fetchAllDeputies())
        .thenReturn(
            List.of(
                new CamaraDeputy(
                    "204379", "Acácio Favacho", "MDB", "AP", "http://photo", "dep@camara.leg.br", "12345678901", "Superior", List.of())));
    when(senadoGateway.fetchCurrentSenators())
        .thenReturn(List.of(new SenadoSenator("5672", "Alan Rick", "MDB", "AC", "http://senphoto", "sen@senado.leg.br")));
    when(politicianSyncGateway.syncPolitician(eq(mdbId), any())).thenReturn(UUID.randomUUID());

    SyncResult result = service.syncFederalLegislature();

    assertThat(result.partiesSynced()).isEqualTo(1);
    assertThat(result.deputiesSynced()).isEqualTo(1);
    assertThat(result.senatorsSynced()).isEqualTo(1);
    assertThat(result.failures()).isZero();

    verify(politicianSyncGateway)
        .syncPolitician(
            eq(mdbId),
            argThatMatchesDeputy());
    verify(politicianSyncGateway, times(2)).syncPolitician(eq(mdbId), any());
  }

  private static SyncPoliticianCommand argThatMatchesDeputy() {
    return org.mockito.ArgumentMatchers.argThat(
        command -> command != null && "CAMARA_DEPUTADO".equals(command.externalSource()) && "12345678901".equals(command.documentNumber()));
  }

  @Test
  void deputyReferencingUnknownPartyIsSkippedNotFatal() {
    when(camaraGateway.fetchAllParties()).thenReturn(List.of());
    when(camaraGateway.fetchAllDeputies())
        .thenReturn(List.of(new CamaraDeputy("1", "Someone", "GHOST", "SP", null, null, null, null, List.of())));
    when(senadoGateway.fetchCurrentSenators()).thenReturn(List.of());

    SyncResult result = service.syncFederalLegislature();

    assertThat(result.deputiesSynced()).isZero();
    assertThat(result.failures()).isEqualTo(1);
    verify(politicianSyncGateway, never()).syncPolitician(any(), any());
  }

  @Test
  void senatorFromPartyWithNoChamberSeatTriggersFallbackPartyRegistration() {
    when(camaraGateway.fetchAllParties()).thenReturn(List.of());
    when(camaraGateway.fetchAllDeputies()).thenReturn(List.of());
    UUID fallbackPartyId = UUID.randomUUID();
    when(partySyncGateway.syncParty(any())).thenReturn(fallbackPartyId);
    when(senadoGateway.fetchCurrentSenators())
        .thenReturn(List.of(new SenadoSenator("9", "Small Party Senator", "REDE", "RJ", null, null)));

    SyncResult result = service.syncFederalLegislature();

    assertThat(result.partiesSynced()).isEqualTo(1);
    assertThat(result.senatorsSynced()).isEqualTo(1);
    verify(partySyncGateway).syncParty(argThatMatchesFallbackParty());
    verify(politicianSyncGateway).syncPolitician(eq(fallbackPartyId), any());
  }

  private static SyncPartyCommand argThatMatchesFallbackParty() {
    return org.mockito.ArgumentMatchers.argThat(command -> command != null && "SENADO_PARTIDO_FALLBACK".equals(command.externalSource()));
  }

  @Test
  void senatorWithoutCpfSourceAlwaysGetsSyntheticDocumentNumber() {
    UUID redeId = UUID.randomUUID();
    when(camaraGateway.fetchAllParties()).thenReturn(List.of(new CamaraParty("1", "REDE", "Rede Sustentabilidade", null, 18)));
    when(partySyncGateway.syncParty(any())).thenReturn(redeId);
    when(camaraGateway.fetchAllDeputies()).thenReturn(List.of());
    when(senadoGateway.fetchCurrentSenators())
        .thenReturn(List.of(new SenadoSenator("9", "Some Senator", "REDE", "RJ", null, null)));

    service.syncFederalLegislature();

    verify(politicianSyncGateway)
        .syncPolitician(
            eq(redeId),
            org.mockito.ArgumentMatchers.argThat(command -> command.documentNumber() != null && command.documentNumber().length() == 11));
  }
}
