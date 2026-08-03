package dev.civicpulse.directory.adapter.in.web;

import dev.civicpulse.directory.adapter.in.web.dto.PartyResponse;
import dev.civicpulse.directory.adapter.in.web.dto.UpdatePartyDetailsRequest;
import dev.civicpulse.directory.adapter.in.web.dto.UpdateProfileImagesRequest;
import dev.civicpulse.directory.application.port.in.SearchDirectoryUseCase;
import dev.civicpulse.directory.application.port.in.UpdatePartyDetailsUseCase;
import dev.civicpulse.directory.application.port.in.UpdateProfileImagesUseCase;
import dev.civicpulse.directory.domain.model.PartySpectrum;
import jakarta.validation.Valid;
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
  private final UpdatePartyDetailsUseCase updatePartyDetailsUseCase;

  public PartyController(
      SearchDirectoryUseCase searchDirectoryUseCase,
      UpdateProfileImagesUseCase updateProfileImagesUseCase,
      UpdatePartyDetailsUseCase updatePartyDetailsUseCase) {
    this.searchDirectoryUseCase = searchDirectoryUseCase;
    this.updateProfileImagesUseCase = updateProfileImagesUseCase;
    this.updatePartyDetailsUseCase = updatePartyDetailsUseCase;
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

  /** Self-service only, same convention as {@code /profile-images} above — see
   * {@code Party.updateDetails}'s javadoc for why a party can edit fields that are normally
   * government-sync-owned. */
  @PatchMapping("/details")
  public ResponseEntity<Void> updateDetails(
      @RequestHeader("X-Account-Id") UUID partyId, @Valid @RequestBody UpdatePartyDetailsRequest request) {
    updatePartyDetailsUseCase.updatePartyDetails(
        partyId, request.name(), request.acronym(), request.number(), request.ideology(), request.foundedYear(), request.president());
    return ResponseEntity.noContent().build();
  }

  @GetMapping
  public List<PartyResponse> search(
      @RequestParam(required = false) String spectrum,
      @RequestParam(required = false) String q,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int pageSize) {
    PartySpectrum partySpectrum = spectrum == null ? null : PartySpectrum.fromCode(spectrum);
    return searchDirectoryUseCase.searchParties(partySpectrum, q, page, pageSize).stream().map(PartyResponse::from).toList();
  }
}
