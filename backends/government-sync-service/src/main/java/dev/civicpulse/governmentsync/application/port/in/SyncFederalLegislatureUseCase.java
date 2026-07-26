package dev.civicpulse.governmentsync.application.port.in;

public interface SyncFederalLegislatureUseCase {

  SyncResult syncFederalLegislature();

  record SyncResult(int partiesSynced, int deputiesSynced, int senatorsSynced, int failures) {}
}
