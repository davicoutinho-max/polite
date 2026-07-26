package dev.civicpulse.partymanagement.adapter.in.web;

import dev.civicpulse.partymanagement.adapter.in.web.dto.RepresentativeResponse;
import dev.civicpulse.partymanagement.adapter.in.web.dto.SyncPoliticianRequest;
import dev.civicpulse.partymanagement.application.port.in.SyncPoliticianUseCase;
import dev.civicpulse.partymanagement.application.port.in.SyncPoliticianUseCase.SyncPoliticianCommand;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Internal-only: not routed to the public internet by the Gateway (see gateway-service's
 * RouteConfig). Called by government-sync-service's federal/state/municipal sync jobs — a
 * separate top-level resource from {@link RepresentativeController} because the target party is
 * part of the sync payload itself, not known from the URL ahead of time (a politician's party can
 * change between sync runs). */
@RestController
@RequestMapping("/politicians")
public class PoliticianSyncController {

  private final SyncPoliticianUseCase syncPoliticianUseCase;

  public PoliticianSyncController(SyncPoliticianUseCase syncPoliticianUseCase) {
    this.syncPoliticianUseCase = syncPoliticianUseCase;
  }

  @PostMapping("/sync")
  public RepresentativeResponse sync(@Valid @RequestBody SyncPoliticianRequest request) {
    var representative =
        syncPoliticianUseCase.syncPolitician(
            request.partyId(),
            new SyncPoliticianCommand(
                request.name(),
                request.handle(),
                request.email(),
                request.avatarUrl(),
                request.documentType(),
                request.documentNumber(),
                request.externalSource(),
                request.externalId(),
                request.roleTitle(),
                request.state(),
                request.govLevel()));
    return RepresentativeResponse.from(representative);
  }
}
