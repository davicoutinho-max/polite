package dev.civicpulse.legislative.adapter.in.web;

import dev.civicpulse.legislative.adapter.in.web.dto.AddMandateRequest;
import dev.civicpulse.legislative.adapter.in.web.dto.AddSocialLinkRequest;
import dev.civicpulse.legislative.adapter.in.web.dto.AddTeamMemberRequest;
import dev.civicpulse.legislative.adapter.in.web.dto.DossierResponse;
import dev.civicpulse.legislative.adapter.in.web.dto.UpdateActivityCountsRequest;
import dev.civicpulse.legislative.adapter.in.web.dto.MandateResponse;
import dev.civicpulse.legislative.adapter.in.web.dto.SocialLinkResponse;
import dev.civicpulse.legislative.adapter.in.web.dto.TeamMemberResponse;
import dev.civicpulse.legislative.adapter.in.web.dto.UpdateDossierRequest;
import dev.civicpulse.legislative.application.port.in.PoliticianDossierUseCase;
import dev.civicpulse.legislative.domain.exception.NotDossierOwnerException;
import dev.civicpulse.legislative.domain.model.SocialPlatform;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/politicians/{politicianAccountId}")
public class PoliticianDossierController {

  private final PoliticianDossierUseCase dossierUseCase;

  public PoliticianDossierController(PoliticianDossierUseCase dossierUseCase) {
    this.dossierUseCase = dossierUseCase;
  }

  @GetMapping("/dossier")
  public DossierResponse getDossier(@PathVariable UUID politicianAccountId) {
    return DossierResponse.from(dossierUseCase.getDossier(politicianAccountId));
  }

  /** Self-service only — writes require the gateway-validated caller to be the same politician
   * the dossier belongs to (see NotDossierOwnerException); reads above stay public. */
  @PutMapping("/dossier")
  public DossierResponse updateDossier(
      @PathVariable UUID politicianAccountId, @RequestHeader("X-Account-Id") UUID accountId, @RequestBody UpdateDossierRequest request) {
    requireOwner(politicianAccountId, accountId);
    return DossierResponse.from(
        dossierUseCase.updateDossier(
            politicianAccountId, request.education(), request.profession(), request.patrimony(), request.email(), request.phone(), request.officeDetail()));
  }

  /** Self-service only — same ownership check as updateDossier. */
  @PutMapping("/activity-counts")
  public DossierResponse updateActivityCounts(
      @PathVariable UUID politicianAccountId, @RequestHeader("X-Account-Id") UUID accountId, @Valid @RequestBody UpdateActivityCountsRequest request) {
    requireOwner(politicianAccountId, accountId);
    return DossierResponse.from(
        dossierUseCase.updateActivityCounts(politicianAccountId, request.speechesCount(), request.interviewsCount(), request.tripsCount()));
  }

  @GetMapping("/mandates")
  public List<MandateResponse> getMandates(@PathVariable UUID politicianAccountId) {
    return dossierUseCase.getMandates(politicianAccountId).stream().map(MandateResponse::from).toList();
  }

  @PostMapping("/mandates")
  @ResponseStatus(HttpStatus.CREATED)
  public MandateResponse addMandate(
      @PathVariable UUID politicianAccountId, @RequestHeader("X-Account-Id") UUID accountId, @Valid @RequestBody AddMandateRequest request) {
    requireOwner(politicianAccountId, accountId);
    return MandateResponse.from(dossierUseCase.addMandate(politicianAccountId, request.role(), request.period(), request.current()));
  }

  @GetMapping("/social-links")
  public List<SocialLinkResponse> getSocialLinks(@PathVariable UUID politicianAccountId) {
    return dossierUseCase.getSocialLinks(politicianAccountId).stream().map(SocialLinkResponse::from).toList();
  }

  @PostMapping("/social-links")
  @ResponseStatus(HttpStatus.CREATED)
  public SocialLinkResponse addSocialLink(
      @PathVariable UUID politicianAccountId, @RequestHeader("X-Account-Id") UUID accountId, @Valid @RequestBody AddSocialLinkRequest request) {
    requireOwner(politicianAccountId, accountId);
    return SocialLinkResponse.from(
        dossierUseCase.addSocialLink(
            politicianAccountId, SocialPlatform.fromCode(request.platform()), request.label(), request.handle(), request.url()));
  }

  @GetMapping("/team")
  public List<TeamMemberResponse> getTeamMembers(@PathVariable UUID politicianAccountId) {
    return dossierUseCase.getTeamMembers(politicianAccountId).stream().map(TeamMemberResponse::from).toList();
  }

  @PostMapping("/team")
  @ResponseStatus(HttpStatus.CREATED)
  public TeamMemberResponse addTeamMember(
      @PathVariable UUID politicianAccountId, @RequestHeader("X-Account-Id") UUID accountId, @Valid @RequestBody AddTeamMemberRequest request) {
    requireOwner(politicianAccountId, accountId);
    return TeamMemberResponse.from(dossierUseCase.addTeamMember(politicianAccountId, request.name(), request.role(), request.avatarUrl()));
  }

  private static void requireOwner(UUID politicianAccountId, UUID callerAccountId) {
    if (!politicianAccountId.equals(callerAccountId)) {
      throw new NotDossierOwnerException(politicianAccountId);
    }
  }
}
