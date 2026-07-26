package dev.civicpulse.governmentsync.adapter.in.web;

import dev.civicpulse.governmentsync.adapter.in.web.dto.FederalSyncResponse;
import dev.civicpulse.governmentsync.application.port.in.SyncFederalLegislatureUseCase;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Manual/on-demand trigger for the same sync the {@code FederalSyncScheduler} runs nightly —
 * useful for ops testing and for forcing a refresh without waiting for 4am. Not routed through
 * the Gateway (this service has no public routes at all — see gateway-service's RouteConfig,
 * which lists government-sync-service among the internal-only services): the platform has no
 * permission-gating infrastructure yet (no service reads the Gateway's X-Account-Permissions
 * header today), so exposing an unauthenticated "resync everything" endpoint to the internet
 * would be a real hole rather than the "platform-admin gated" endpoint the plan called for.
 * Reachable directly (e.g. {@code curl localhost:8100/sync/federal/run}) until that
 * infrastructure exists. */
@RestController
@RequestMapping("/sync/federal")
public class FederalSyncController {

  private final SyncFederalLegislatureUseCase syncFederalLegislatureUseCase;

  public FederalSyncController(SyncFederalLegislatureUseCase syncFederalLegislatureUseCase) {
    this.syncFederalLegislatureUseCase = syncFederalLegislatureUseCase;
  }

  @PostMapping("/run")
  public FederalSyncResponse run() {
    return FederalSyncResponse.from(syncFederalLegislatureUseCase.syncFederalLegislature());
  }
}
