package dev.civicpulse.partymanagement.adapter.in.web;

import dev.civicpulse.partymanagement.adapter.in.web.dto.ChangeMemberStatusRequest;
import dev.civicpulse.partymanagement.adapter.in.web.dto.PartyMemberResponse;
import dev.civicpulse.partymanagement.application.port.in.ManagePartyMembershipUseCase;
import dev.civicpulse.partymanagement.domain.model.PartyMemberStatus;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/parties/{partyId}/members")
public class PartyMembershipController {

  private final ManagePartyMembershipUseCase managePartyMembershipUseCase;

  public PartyMembershipController(ManagePartyMembershipUseCase managePartyMembershipUseCase) {
    this.managePartyMembershipUseCase = managePartyMembershipUseCase;
  }

  @GetMapping
  public List<PartyMemberResponse> list(@PathVariable UUID partyId) {
    return managePartyMembershipUseCase.listByParty(partyId).stream().map(PartyMemberResponse::from).toList();
  }

  @PatchMapping("/{citizenAccountId}/status")
  public PartyMemberResponse changeStatus(
      @PathVariable UUID partyId, @PathVariable UUID citizenAccountId, @Valid @RequestBody ChangeMemberStatusRequest request) {
    return PartyMemberResponse.from(
        managePartyMembershipUseCase.changeStatus(partyId, citizenAccountId, PartyMemberStatus.fromCode(request.status())));
  }
}
