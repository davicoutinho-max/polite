package dev.civicpulse.platformconfig.adapter.in.web;

import dev.civicpulse.platformconfig.adapter.in.web.dto.IssuePartyInviteRequest;
import dev.civicpulse.platformconfig.adapter.in.web.dto.PartyInviteResponse;
import dev.civicpulse.platformconfig.application.port.in.ManagePartyInviteUseCase;
import dev.civicpulse.platformconfig.application.port.in.ManagePartyInviteUseCase.PartyInvitePrefill;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Platform-admin only — see docs/architecture for why party accounts are never self-service
 * beyond redeeming an admin-vetted invite (ManagePartyInviteUseCase's javadoc). The gateway is
 * trusted to only route here for accounts with the {@code platform-admin} permission, same
 * convention as every other admin-style endpoint in this system. */
@RestController
@RequestMapping("/party-invites")
public class PartyInviteController {

  private final ManagePartyInviteUseCase useCase;

  public PartyInviteController(ManagePartyInviteUseCase useCase) {
    this.useCase = useCase;
  }

  @PostMapping
  public PartyInviteResponse issue(@RequestHeader("X-Account-Id") UUID adminAccountId, @Valid @RequestBody IssuePartyInviteRequest request) {
    var prefill =
        new PartyInvitePrefill(request.name(), request.acronym(), request.number(), request.ideology(), request.president(), request.cnpj());
    return PartyInviteResponse.from(useCase.issue(adminAccountId, request.targetEmail(), prefill));
  }

  @PostMapping("/{id}/resend")
  public PartyInviteResponse resend(@PathVariable UUID id, @RequestHeader("X-Account-Id") UUID adminAccountId) {
    return PartyInviteResponse.from(useCase.resend(id, adminAccountId));
  }

  @GetMapping
  public List<PartyInviteResponse> list(@RequestHeader("X-Account-Id") UUID adminAccountId) {
    return useCase.listIssuedBy(adminAccountId).stream().map(PartyInviteResponse::from).toList();
  }
}
