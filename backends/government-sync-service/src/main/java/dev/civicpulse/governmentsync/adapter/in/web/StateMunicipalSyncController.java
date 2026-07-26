package dev.civicpulse.governmentsync.adapter.in.web;

import dev.civicpulse.governmentsync.adapter.in.web.dto.StateMunicipalSyncResponse;
import dev.civicpulse.governmentsync.application.port.in.SyncStateAndMunicipalUseCase;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Manual/on-demand trigger for a single state's sync — same "not routed through the Gateway"
 * reasoning as {@code FederalSyncController}. {@code uf} is mandatory and singular by design: the
 * nationwide run this data volume implies (~57k municipal candidates alone) belongs to
 * {@code StateMunicipalSyncScheduler}'s controlled, one-state-at-a-time loop, not to an endpoint
 * that could be fat-fingered into fetching all 27 states' multi-hundred-MB datasets from a single
 * request. */
@RestController
@RequestMapping("/sync/state-municipal")
public class StateMunicipalSyncController {

  private final SyncStateAndMunicipalUseCase syncStateAndMunicipalUseCase;

  public StateMunicipalSyncController(SyncStateAndMunicipalUseCase syncStateAndMunicipalUseCase) {
    this.syncStateAndMunicipalUseCase = syncStateAndMunicipalUseCase;
  }

  @PostMapping("/run")
  public StateMunicipalSyncResponse run(@RequestParam String uf) {
    return StateMunicipalSyncResponse.from(syncStateAndMunicipalUseCase.syncStateAndMunicipal(uf));
  }
}
