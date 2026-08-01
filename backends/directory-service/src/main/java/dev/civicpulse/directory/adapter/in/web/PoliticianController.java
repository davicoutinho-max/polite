package dev.civicpulse.directory.adapter.in.web;

import dev.civicpulse.directory.adapter.in.web.dto.PoliticianResponse;
import dev.civicpulse.directory.adapter.in.web.dto.UpdateProfileImagesRequest;
import dev.civicpulse.directory.application.port.in.SearchDirectoryUseCase;
import dev.civicpulse.directory.application.port.in.UpdateProfileImagesUseCase;
import dev.civicpulse.directory.domain.model.GovLevel;
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
@RequestMapping("/politicians")
public class PoliticianController {

  private final SearchDirectoryUseCase searchDirectoryUseCase;
  private final UpdateProfileImagesUseCase updateProfileImagesUseCase;

  public PoliticianController(SearchDirectoryUseCase searchDirectoryUseCase, UpdateProfileImagesUseCase updateProfileImagesUseCase) {
    this.searchDirectoryUseCase = searchDirectoryUseCase;
    this.updateProfileImagesUseCase = updateProfileImagesUseCase;
  }

  @GetMapping("/{accountId}")
  public PoliticianResponse getById(@PathVariable UUID accountId) {
    return PoliticianResponse.from(searchDirectoryUseCase.getPolitician(accountId));
  }

  /** Self-service only — {@code accountId} always comes from the gateway-validated session
   * header, never the request body, so a politician can only ever update their own photos. */
  @PatchMapping("/profile-images")
  public ResponseEntity<Void> updateProfileImages(
      @RequestHeader("X-Account-Id") UUID accountId, @RequestBody UpdateProfileImagesRequest request) {
    updateProfileImagesUseCase.updatePoliticianProfileImages(accountId, request.avatarUrl(), request.coverImageUrl());
    return ResponseEntity.noContent().build();
  }

  @GetMapping
  public List<PoliticianResponse> search(
      @RequestParam(required = false) String state,
      @RequestParam(required = false) String level,
      @RequestParam(required = false) UUID partyId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int pageSize) {
    GovLevel govLevel = level == null ? null : GovLevel.fromCode(level);
    return searchDirectoryUseCase.searchPoliticians(state, govLevel, partyId, page, pageSize).stream()
        .map(PoliticianResponse::from)
        .toList();
  }
}
