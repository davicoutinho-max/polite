package dev.civicpulse.directory.adapter.in.web;

import dev.civicpulse.directory.adapter.in.web.dto.PartyResponse;
import dev.civicpulse.directory.adapter.in.web.dto.UpdateProfileImagesRequest;
import dev.civicpulse.directory.application.port.in.SearchDirectoryUseCase;
import dev.civicpulse.directory.application.port.in.UpdateProfileImagesUseCase;
import dev.civicpulse.directory.domain.model.PartySpectrum;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/parties")
public class PartyController {

  private final SearchDirectoryUseCase searchDirectoryUseCase;
  private final UpdateProfileImagesUseCase updateProfileImagesUseCase;

  public PartyController(SearchDirectoryUseCase searchDirectoryUseCase, UpdateProfileImagesUseCase updateProfileImagesUseCase) {
    this.searchDirectoryUseCase = searchDirectoryUseCase;
    this.updateProfileImagesUseCase = updateProfileImagesUseCase;
  }

  @GetMapping("/{id}")
  public PartyResponse getById(@PathVariable UUID id) {
    return PartyResponse.from(searchDirectoryUseCase.getParty(id));
  }

  /** Self-service only — the party account's own id (from the gateway-validated session header)
   * doubles as its {@code partyId}, the same 1:1 relationship {@code Politician.accountId} has
   * with a politician's identity account. Logo only — cover photo is edited through
   * party-management-service's party-profile endpoint instead (see {@code UpdatePartyProfileRequest}). */
  @PatchMapping("/profile-images")
  public ResponseEntity<Void> updateProfileImages(
      @RequestHeader("X-Account-Id") UUID partyId, @RequestBody UpdateProfileImagesRequest request) {
    updateProfileImagesUseCase.updatePartyLogo(partyId, request.avatarUrl());
    return ResponseEntity.noContent().build();
  }

  @GetMapping
  public List<PartyResponse> search(
      @RequestParam(required = false) String spectrum,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int pageSize) {
    PartySpectrum partySpectrum = spectrum == null ? null : PartySpectrum.fromCode(spectrum);
    return searchDirectoryUseCase.searchParties(partySpectrum, page, pageSize).stream().map(PartyResponse::from).toList();
  }
}
