package dev.civicpulse.governmentsync.adapter.in.scheduler;

import dev.civicpulse.governmentsync.application.port.in.SyncFederalLegislatureUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
class FederalSyncScheduler {

  private static final Logger log = LoggerFactory.getLogger(FederalSyncScheduler.class);

  private final SyncFederalLegislatureUseCase syncFederalLegislatureUseCase;

  FederalSyncScheduler(SyncFederalLegislatureUseCase syncFederalLegislatureUseCase) {
    this.syncFederalLegislatureUseCase = syncFederalLegislatureUseCase;
  }

  /** Daily at 4am — Câmara/Senado data changes rarely (a party switch, a new term), so this is
   * about freshness margin, not load. See FederalSyncController for the on-demand trigger. */
  @Scheduled(cron = "0 0 4 * * *")
  void runDailySync() {
    log.info("Starting scheduled federal legislature sync");
    syncFederalLegislatureUseCase.syncFederalLegislature();
  }
}
