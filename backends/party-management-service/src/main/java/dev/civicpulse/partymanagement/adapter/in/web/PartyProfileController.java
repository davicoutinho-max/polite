package dev.civicpulse.partymanagement.adapter.in.web;

import dev.civicpulse.partymanagement.adapter.in.web.dto.PartyProfileResponse;
import dev.civicpulse.partymanagement.adapter.in.web.dto.UpdatePartyProfileRequest;
import dev.civicpulse.partymanagement.application.port.in.ManagePartyProfileUseCase;
import dev.civicpulse.partymanagement.domain.exception.NotPartyProfileOwnerException;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/parties/{partyId}/profile")
public class PartyProfileController {

  private final ManagePartyProfileUseCase managePartyProfileUseCase;

  public PartyProfileController(ManagePartyProfileUseCase managePartyProfileUseCase) {
    this.managePartyProfileUseCase = managePartyProfileUseCase;
  }

  @GetMapping
  public PartyProfileResponse get(@PathVariable UUID partyId) {
    return PartyProfileResponse.from(managePartyProfileUseCase.getProfile(partyId));
  }

  /** Self-service only — writes require the gateway-validated caller to be the same party the
   * profile belongs to (see NotPartyProfileOwnerException); the read above stays public. */
  @PutMapping
  public PartyProfileResponse update(
      @PathVariable UUID partyId, @RequestHeader("X-Account-Id") UUID accountId, @RequestBody UpdatePartyProfileRequest request) {
    if (!partyId.equals(accountId)) {
      throw new NotPartyProfileOwnerException(partyId);
    }
    return PartyProfileResponse.from(
        managePartyProfileUseCase.updateProfile(
            partyId, request.history(), request.program(), request.statuteUrl(), request.coverUrl(), request.videoUrl()));
  }
}
