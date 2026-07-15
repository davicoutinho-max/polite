package dev.civicpulse.membershipaffiliation.adapter.in.web;

import dev.civicpulse.membershipaffiliation.adapter.in.web.dto.AffiliationResponse;
import dev.civicpulse.membershipaffiliation.adapter.in.web.dto.MembershipCardResponse;
import dev.civicpulse.membershipaffiliation.adapter.in.web.dto.RequestAffiliationRequest;
import dev.civicpulse.membershipaffiliation.application.port.in.ManageAffiliationUseCase;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/affiliations")
public class AffiliationController {

  private final ManageAffiliationUseCase manageAffiliationUseCase;

  public AffiliationController(ManageAffiliationUseCase manageAffiliationUseCase) {
    this.manageAffiliationUseCase = manageAffiliationUseCase;
  }

  /** {@code X-Account-Id} is the citizen's own id, forwarded by the Gateway after JWT
   * validation — see docs/architecture/system-architecture.html. */
  @PostMapping
  public ResponseEntity<AffiliationResponse> request(
      @RequestHeader("X-Account-Id") UUID citizenAccountId, @Valid @RequestBody RequestAffiliationRequest request) {
    var affiliation = manageAffiliationUseCase.requestAffiliation(citizenAccountId, request.partyId(), request.city());
    AffiliationResponse body = AffiliationResponse.from(affiliation);
    return ResponseEntity.created(URI.create("/affiliations/" + body.id())).body(body);
  }

  @GetMapping("/{id}")
  public AffiliationResponse getById(@PathVariable UUID id) {
    return AffiliationResponse.from(manageAffiliationUseCase.getById(id));
  }

  @GetMapping
  public List<AffiliationResponse> listByCitizen(@RequestParam UUID citizenAccountId) {
    return manageAffiliationUseCase.listByCitizen(citizenAccountId).stream().map(AffiliationResponse::from).toList();
  }

  /** Simulates the external Electoral Justice authority's intake (no real integration exists —
   * see the use case's javadoc). */
  @PostMapping("/{id}/send-to-electoral-justice")
  public AffiliationResponse sendToElectoralJustice(@PathVariable UUID id) {
    return AffiliationResponse.from(manageAffiliationUseCase.sendToElectoralJustice(id));
  }

  @PostMapping("/{id}/confirm")
  public AffiliationResponse confirm(@PathVariable UUID id) {
    return AffiliationResponse.from(manageAffiliationUseCase.confirmAffiliation(id));
  }

  @GetMapping("/{id}/card")
  public ResponseEntity<MembershipCardResponse> getCard(@PathVariable UUID id) {
    return manageAffiliationUseCase.getCard(id).map(card -> ResponseEntity.ok(MembershipCardResponse.from(card))).orElseGet(() -> ResponseEntity.notFound().build());
  }
}
