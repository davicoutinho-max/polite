package dev.civicpulse.identity.adapter.in.web;

import dev.civicpulse.identity.adapter.in.web.dto.IssueRegistrationTokenRequest;
import dev.civicpulse.identity.adapter.in.web.dto.RegistrationTokenResponse;
import dev.civicpulse.identity.application.port.in.IssueRegistrationTokenUseCase;
import dev.civicpulse.identity.application.port.in.RedeemRegistrationTokenUseCase;
import dev.civicpulse.identity.domain.model.AccountType;
import dev.civicpulse.identity.domain.model.RegistrationToken;
import jakarta.validation.Valid;
import java.time.Clock;
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

/** Internal-only, not routed to the public internet by the Gateway, EXCEPT {@code
 * GET /registration-tokens/validate} which is deliberately public — the register page needs to
 * preview an invite before the visitor has an account or session of any kind. Issuing/resending/
 * listing are called server-to-server by platform-configuration-service (party invites) and
 * party-management-service (politician invites); redeeming likewise happens from those services
 * right before they create the actual account — see RegistrationToken's javadoc. */
@RestController
@RequestMapping("/registration-tokens")
public class RegistrationTokenController {

  private final IssueRegistrationTokenUseCase issueUseCase;
  private final RedeemRegistrationTokenUseCase redeemUseCase;
  private final Clock clock;

  public RegistrationTokenController(IssueRegistrationTokenUseCase issueUseCase, RedeemRegistrationTokenUseCase redeemUseCase, Clock clock) {
    this.issueUseCase = issueUseCase;
    this.redeemUseCase = redeemUseCase;
    this.clock = clock;
  }

  @PostMapping
  public ResponseEntity<RegistrationTokenResponse> issue(@Valid @RequestBody IssueRegistrationTokenRequest request) {
    RegistrationToken token =
        issueUseCase.issue(AccountType.fromCode(request.accountType()), request.issuedByAccountId(), request.targetEmail(), request.prefillData());
    return ResponseEntity.ok(RegistrationTokenResponse.from(token, clock.instant()));
  }

  @PostMapping("/{id}/resend")
  public RegistrationTokenResponse resend(@PathVariable UUID id, @RequestParam UUID issuedByAccountId) {
    RegistrationToken token = issueUseCase.resend(id, issuedByAccountId);
    return RegistrationTokenResponse.from(token, clock.instant());
  }

  @GetMapping
  public List<RegistrationTokenResponse> listIssuedBy(@RequestParam UUID issuedByAccountId) {
    return issueUseCase.listIssuedBy(issuedByAccountId).stream().map(t -> RegistrationTokenResponse.from(t, clock.instant())).toList();
  }

  /** Public, read-only — see the class javadoc. */
  @GetMapping("/validate")
  public RegistrationTokenResponse validate(@RequestParam String token) {
    return RegistrationTokenResponse.from(redeemUseCase.validate(token), clock.instant());
  }

  @PostMapping("/redeem")
  public RegistrationTokenResponse redeem(@RequestParam String token) {
    return RegistrationTokenResponse.from(redeemUseCase.redeem(token), clock.instant());
  }
}
