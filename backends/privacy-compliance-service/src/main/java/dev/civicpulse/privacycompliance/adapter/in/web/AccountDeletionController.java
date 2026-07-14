package dev.civicpulse.privacycompliance.adapter.in.web;

import dev.civicpulse.privacycompliance.adapter.in.web.dto.AccountDeletionRequestResponse;
import dev.civicpulse.privacycompliance.adapter.in.web.dto.ErasureAuditEntryResponse;
import dev.civicpulse.privacycompliance.application.port.in.ManageAccountDeletionUseCase;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/account-deletion-requests")
public class AccountDeletionController {

  private final ManageAccountDeletionUseCase manageAccountDeletionUseCase;

  public AccountDeletionController(ManageAccountDeletionUseCase manageAccountDeletionUseCase) {
    this.manageAccountDeletionUseCase = manageAccountDeletionUseCase;
  }

  /** {@code X-Account-Id} is the caller's own id, forwarded by the Gateway after JWT
   * validation — see docs/architecture/system-architecture.html. */
  @PostMapping
  public ResponseEntity<AccountDeletionRequestResponse> request(@RequestHeader("X-Account-Id") UUID accountId) {
    AccountDeletionRequestResponse body = AccountDeletionRequestResponse.from(manageAccountDeletionUseCase.request(accountId));
    return ResponseEntity.created(URI.create("/account-deletion-requests/" + body.id())).body(body);
  }

  @GetMapping("/{id}")
  public AccountDeletionRequestResponse getById(@PathVariable UUID id) {
    return AccountDeletionRequestResponse.from(manageAccountDeletionUseCase.getById(id));
  }

  @GetMapping
  public List<AccountDeletionRequestResponse> listByAccount(@RequestHeader("X-Account-Id") UUID accountId) {
    return manageAccountDeletionUseCase.listByAccount(accountId).stream().map(AccountDeletionRequestResponse::from).toList();
  }

  /** A reconfirmation step (common LGPD/"cooling-off" pattern) before the erasure saga actually
   * starts. */
  @PostMapping("/{id}/confirm")
  public AccountDeletionRequestResponse confirm(@PathVariable UUID id) {
    return AccountDeletionRequestResponse.from(manageAccountDeletionUseCase.confirm(id));
  }

  @PostMapping("/{id}/start-processing")
  public AccountDeletionRequestResponse startProcessing(@PathVariable UUID id) {
    return AccountDeletionRequestResponse.from(manageAccountDeletionUseCase.startProcessing(id));
  }

  @PostMapping("/{id}/cancel")
  public AccountDeletionRequestResponse cancel(@PathVariable UUID id) {
    return AccountDeletionRequestResponse.from(manageAccountDeletionUseCase.cancel(id));
  }

  @GetMapping("/{id}/erasure-audit")
  public List<ErasureAuditEntryResponse> listErasureAudit(@PathVariable UUID id) {
    return manageAccountDeletionUseCase.listErasureAudit(id).stream().map(ErasureAuditEntryResponse::from).toList();
  }
}
