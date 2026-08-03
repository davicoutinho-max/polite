package dev.civicpulse.partymanagement.adapter.in.web;

import dev.civicpulse.partymanagement.adapter.in.web.dto.IssuePoliticianInviteRequest;
import dev.civicpulse.partymanagement.adapter.in.web.dto.PoliticianInviteResponse;
import dev.civicpulse.partymanagement.application.port.in.ManagePoliticianInviteUseCase;
import dev.civicpulse.partymanagement.application.port.in.ManagePoliticianInviteUseCase.PoliticianInvitePrefill;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Self-service — the {@code partyId} path segment doubles as the caller's own account id, same
 * convention as every other party-scoped write in this service (see RepresentativeController).
 * Replaces the old "party types the new politician's password directly" step of flow 02 — see
 * ManagePoliticianInviteUseCase's javadoc. */
@RestController
@RequestMapping("/parties/{partyId}/politician-invites")
public class PoliticianInviteController {

  private final ManagePoliticianInviteUseCase useCase;

  public PoliticianInviteController(ManagePoliticianInviteUseCase useCase) {
    this.useCase = useCase;
  }

  @PostMapping
  public PoliticianInviteResponse issue(@PathVariable UUID partyId, @Valid @RequestBody IssuePoliticianInviteRequest request) {
    var prefill = new PoliticianInvitePrefill(request.name(), request.roleTitle(), request.state(), partyId);
    return PoliticianInviteResponse.from(useCase.issue(partyId, request.targetEmail(), prefill));
  }

  @PostMapping("/{id}/resend")
  public PoliticianInviteResponse resend(@PathVariable UUID partyId, @PathVariable UUID id) {
    return PoliticianInviteResponse.from(useCase.resend(partyId, id));
  }

  @GetMapping
  public List<PoliticianInviteResponse> list(@PathVariable UUID partyId) {
    return useCase.listIssuedBy(partyId).stream().map(PoliticianInviteResponse::from).toList();
  }
}
