package dev.civicpulse.identity.adapter.in.web;

import dev.civicpulse.identity.adapter.in.web.dto.AuthResponse;
import dev.civicpulse.identity.adapter.in.web.dto.LoginRequest;
import dev.civicpulse.identity.adapter.in.web.dto.RefreshRequest;
import dev.civicpulse.identity.application.port.in.AuthenticateUseCase;
import dev.civicpulse.identity.application.port.in.AuthenticateUseCase.LoginCommand;
import dev.civicpulse.identity.application.port.in.ManageSessionUseCase;
import dev.civicpulse.identity.domain.model.AccountId;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

  private final AuthenticateUseCase authenticateUseCase;
  private final ManageSessionUseCase manageSessionUseCase;

  public AuthController(AuthenticateUseCase authenticateUseCase, ManageSessionUseCase manageSessionUseCase) {
    this.authenticateUseCase = authenticateUseCase;
    this.manageSessionUseCase = manageSessionUseCase;
  }

  @PostMapping("/login")
  public AuthResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
    var result =
        authenticateUseCase.login(
            new LoginCommand(request.email(), request.password(), httpRequest.getHeader("User-Agent"), clientIp(httpRequest)));
    return AuthResponse.from(result);
  }

  @PostMapping("/refresh")
  public AuthResponse refresh(@Valid @RequestBody RefreshRequest request, HttpServletRequest httpRequest) {
    var result = manageSessionUseCase.refresh(request.refreshToken(), httpRequest.getHeader("User-Agent"), clientIp(httpRequest));
    return AuthResponse.from(result);
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest request) {
    manageSessionUseCase.revoke(request.refreshToken());
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/accounts/{accountId}/logout-all")
  public ResponseEntity<Void> logoutAll(@PathVariable java.util.UUID accountId) {
    manageSessionUseCase.revokeAll(AccountId.of(accountId));
    return ResponseEntity.noContent().build();
  }

  private static String clientIp(HttpServletRequest request) {
    String forwardedFor = request.getHeader("X-Forwarded-For");
    if (forwardedFor != null && !forwardedFor.isBlank()) {
      return forwardedFor.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }
}
