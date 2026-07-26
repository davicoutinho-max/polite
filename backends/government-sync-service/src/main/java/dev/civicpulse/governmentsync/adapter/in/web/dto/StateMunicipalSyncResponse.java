package dev.civicpulse.governmentsync.adapter.in.web.dto;

import dev.civicpulse.governmentsync.application.port.in.SyncStateAndMunicipalUseCase.SyncResult;

public record StateMunicipalSyncResponse(int partiesSynced, int stateSynced, int municipalSynced, int failures) {

  public static StateMunicipalSyncResponse from(SyncResult result) {
    return new StateMunicipalSyncResponse(result.partiesSynced(), result.stateSynced(), result.municipalSynced(), result.failures());
  }
}
