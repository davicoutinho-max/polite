package dev.civicpulse.identity.adapter.in.web;

import dev.civicpulse.identity.adapter.in.web.dto.AccountResponse;
import dev.civicpulse.identity.adapter.in.web.dto.ProvisionAccountRequest;
import dev.civicpulse.identity.adapter.in.web.dto.RegisterAccountRequest;
import dev.civicpulse.identity.application.port.in.GetAccountUseCase;
import dev.civicpulse.identity.application.port.in.RegisterAccountUseCase;
import dev.civicpulse.identity.application.port.in.RegisterAccountUseCase.RegisterAccountCommand;
import dev.civicpulse.identity.application.port.in.VerifyDocumentUseCase;
import dev.civicpulse.identity.domain.model.Account;
import dev.civicpulse.identity.domain.model.AccountId;
import dev.civicpulse.identity.domain.model.AccountType;
import dev.civicpulse.identity.domain.model.DocumentType;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/accounts")
public class AccountController {

  private final RegisterAccountUseCase registerAccountUseCase;
  private final GetAccountUseCase getAccountUseCase;
  private final VerifyDocumentUseCase verifyDocumentUseCase;

  public AccountController(
      RegisterAccountUseCase registerAccountUseCase,
      GetAccountUseCase getAccountUseCase,
      VerifyDocumentUseCase verifyDocumentUseCase) {
    this.registerAccountUseCase = registerAccountUseCase;
    this.getAccountUseCase = getAccountUseCase;
    this.verifyDocumentUseCase = verifyDocumentUseCase;
  }

  /** Public self-registration — always a citizen account. */
  @PostMapping("/register")
  public ResponseEntity<AccountResponse> register(@Valid @RequestBody RegisterAccountRequest request) {
    Account account =
        registerAccountUseCase.registerCitizen(
            new RegisterAccountCommand(request.name(), request.handle(), request.email(), request.password(), DocumentType.CPF, request.cpf()));
    return created(account);
  }

  /** Internal-only: not routed to the public internet by the Gateway. Called by Party
   * Management when a party registers a politician, or by a platform-admin flow. */
  @PostMapping("/provision")
  public ResponseEntity<AccountResponse> provision(@Valid @RequestBody ProvisionAccountRequest request) {
    AccountType accountType = AccountType.fromCode(request.accountType());
    DocumentType documentType = accountType == AccountType.ADMIN ? null : DocumentType.fromCode(request.documentType());
    Account account =
        registerAccountUseCase.provisionAccount(
            accountType,
            new RegisterAccountCommand(
                request.name(), request.handle(), request.email(), request.password(), documentType, request.documentNumber()));
    return created(account);
  }

  @GetMapping("/{id}")
  public AccountResponse getById(@PathVariable UUID id) {
    return AccountResponse.from(getAccountUseCase.getById(AccountId.of(id)));
  }

  @GetMapping("/{id}/permissions")
  public Set<String> getPermissions(@PathVariable UUID id) {
    return getAccountUseCase.getPermissions(AccountId.of(id));
  }

  @PostMapping("/{id}/verify-document")
  public ResponseEntity<Void> verifyDocument(@PathVariable UUID id) {
    verifyDocumentUseCase.verify(AccountId.of(id));
    return ResponseEntity.noContent().build();
  }

  private ResponseEntity<AccountResponse> created(Account account) {
    AccountResponse body = AccountResponse.from(account);
    return ResponseEntity.created(URI.create("/accounts/" + body.id())).body(body);
  }
}
