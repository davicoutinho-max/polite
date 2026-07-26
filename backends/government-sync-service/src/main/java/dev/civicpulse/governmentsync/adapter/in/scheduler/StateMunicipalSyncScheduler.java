package dev.civicpulse.governmentsync.adapter.in.scheduler;

import dev.civicpulse.governmentsync.application.port.in.SyncStateAndMunicipalUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
class StateMunicipalSyncScheduler {

  private static final Logger log = LoggerFactory.getLogger(StateMunicipalSyncScheduler.class);

  // All 27 Brazilian UFs (26 states + DF) — TSE publishes one CSV entry per UF inside each
  // year's bulk zip (see TseElectionDataGateway).
  private static final String[] ALL_UFS = {
    "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG", "PA", "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR",
    "SC", "SP", "SE", "TO"
  };

  private final SyncStateAndMunicipalUseCase syncStateAndMunicipalUseCase;

  StateMunicipalSyncScheduler(SyncStateAndMunicipalUseCase syncStateAndMunicipalUseCase) {
    this.syncStateAndMunicipalUseCase = syncStateAndMunicipalUseCase;
  }

  /** Monthly, not daily like the federal sync — TSE's bulk datasets only change meaningfully once
   * per election cycle, with occasional post-election corrections/retifications; a whole-Brazil
   * run (~57k municipal candidates alone) is also far too heavy to justify running nightly. Runs
   * one UF fully before starting the next, deliberately sequential — party-management-service and
   * platform-configuration-service take one HTTP call per candidate, and this is already the
   * heaviest load either of them sees from this whole integration. */
  @Scheduled(cron = "0 0 5 1 * *")
  void runMonthlySync() {
    for (String uf : ALL_UFS) {
      log.info("Starting scheduled state/municipal sync for {}", uf);
      syncStateAndMunicipalUseCase.syncStateAndMunicipal(uf);
    }
  }
}
