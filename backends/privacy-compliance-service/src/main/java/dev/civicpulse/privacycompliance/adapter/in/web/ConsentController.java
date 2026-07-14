package dev.civicpulse.privacycompliance.adapter.in.web;

import dev.civicpulse.privacycompliance.adapter.in.web.dto.ConsentRecordResponse;
import dev.civicpulse.privacycompliance.adapter.in.web.dto.UpdateConsentRequest;
import dev.civicpulse.privacycompliance.application.port.in.ManageConsentUseCase;
import dev.civicpulse.privacycompliance.domain.model.ConsentPurpose;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** {@code X-Account-Id} is the caller's own id, forwarded by the Gateway after JWT validation —
 * see docs/architecture/system-architecture.html. */
@RestController
@RequestMapping("/consents")
public class ConsentController {

  private final ManageConsentUseCase manageConsentUseCase;

  public ConsentController(ManageConsentUseCase manageConsentUseCase) {
    this.manageConsentUseCase = manageConsentUseCase;
  }

  @PutMapping
  public ConsentRecordResponse updateConsent(@RequestHeader("X-Account-Id") UUID accountId, @Valid @RequestBody UpdateConsentRequest request) {
    return ConsentRecordResponse.from(manageConsentUseCase.updateConsent(accountId, ConsentPurpose.fromCode(request.purpose()), request.granted()));
  }

  @GetMapping
  public List<ConsentRecordResponse> list(@RequestHeader("X-Account-Id") UUID accountId) {
    return manageConsentUseCase.listByAccount(accountId).stream().map(ConsentRecordResponse::from).toList();
  }
}
