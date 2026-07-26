package dev.civicpulse.governmentsync.application.port.in;

public interface SyncStateAndMunicipalUseCase {

  SyncResult syncStateAndMunicipal(String uf);

  record SyncResult(int partiesSynced, int stateSynced, int municipalSynced, int failures) {}
}
