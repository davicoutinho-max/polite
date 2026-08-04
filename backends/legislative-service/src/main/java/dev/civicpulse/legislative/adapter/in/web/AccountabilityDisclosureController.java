package dev.civicpulse.legislative.adapter.in.web;

import dev.civicpulse.legislative.adapter.in.web.dto.AccountabilityDisclosureResponse;
import dev.civicpulse.legislative.adapter.in.web.dto.SubmitAccountabilityDisclosureRequest;
import dev.civicpulse.legislative.application.port.in.ManageAccountabilityDisclosureUseCase;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccountabilityDisclosureController {

  private final ManageAccountabilityDisclosureUseCase manageAccountabilityDisclosureUseCase;

  public AccountabilityDisclosureController(ManageAccountabilityDisclosureUseCase manageAccountabilityDisclosureUseCase) {
    this.manageAccountabilityDisclosureUseCase = manageAccountabilityDisclosureUseCase;
  }

  /** Self-service only — {@code X-Account-Id} (gateway-validated session header) is always the
   * submitting politician, never a path/body parameter, so a politician can only ever submit
   * disclosures under their own name. The AI review happens synchronously in this call — see
   * ManageAccountabilityDisclosureUseCase.submit's javadoc for why this never returns "pending". */
  @PostMapping("/politicians/accountability-disclosures")
  @ResponseStatus(HttpStatus.CREATED)
  public AccountabilityDisclosureResponse submit(
      @RequestHeader("X-Account-Id") UUID politicianAccountId, @Valid @RequestBody SubmitAccountabilityDisclosureRequest request) {
    var disclosure =
        manageAccountabilityDisclosureUseCase.submit(
            politicianAccountId,
            request.category(),
            request.periodMonth(),
            request.periodYear(),
            request.declaredAmountCents(),
            request.documentUrl(),
            request.notes());
    return AccountabilityDisclosureResponse.from(disclosure);
  }

  /** Public — accountability history is transparency data, same visibility as the rest of
   * TransparencyController. */
  @GetMapping("/politicians/{politicianAccountId}/accountability-disclosures")
  public List<AccountabilityDisclosureResponse> listByPolitician(@PathVariable UUID politicianAccountId) {
    return manageAccountabilityDisclosureUseCase.listByPolitician(politicianAccountId).stream()
        .map(AccountabilityDisclosureResponse::from)
        .toList();
  }
}
