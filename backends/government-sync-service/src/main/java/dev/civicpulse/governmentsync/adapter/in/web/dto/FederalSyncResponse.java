package dev.civicpulse.governmentsync.adapter.in.web.dto;

import dev.civicpulse.governmentsync.application.port.in.SyncFederalLegislatureUseCase.SyncResult;

public record FederalSyncResponse(int partiesSynced, int deputiesSynced, int senatorsSynced, int failures) {

  public static FederalSyncResponse from(SyncResult result) {
    return new FederalSyncResponse(result.partiesSynced(), result.deputiesSynced(), result.senatorsSynced(), result.failures());
  }
}
