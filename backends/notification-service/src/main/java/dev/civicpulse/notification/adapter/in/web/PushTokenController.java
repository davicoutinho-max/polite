package dev.civicpulse.notification.adapter.in.web;

import dev.civicpulse.notification.adapter.in.web.dto.PushTokenResponse;
import dev.civicpulse.notification.adapter.in.web.dto.RegisterPushTokenRequest;
import dev.civicpulse.notification.application.port.in.ManagePushTokenUseCase;
import dev.civicpulse.notification.domain.model.NotificationPlatform;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/push-tokens")
public class PushTokenController {

  private final ManagePushTokenUseCase managePushTokenUseCase;

  public PushTokenController(ManagePushTokenUseCase managePushTokenUseCase) {
    this.managePushTokenUseCase = managePushTokenUseCase;
  }

  @PostMapping
  public PushTokenResponse register(@RequestHeader("X-Account-Id") UUID accountId, @Valid @RequestBody RegisterPushTokenRequest request) {
    return PushTokenResponse.from(managePushTokenUseCase.register(accountId, NotificationPlatform.fromCode(request.platform()), request.token()));
  }

  @GetMapping
  public List<PushTokenResponse> list(@RequestHeader("X-Account-Id") UUID accountId) {
    return managePushTokenUseCase.listByAccount(accountId).stream().map(PushTokenResponse::from).toList();
  }

  @DeleteMapping
  public ResponseEntity<Void> unregister(@RequestHeader("X-Account-Id") UUID accountId, @Valid @RequestBody RegisterPushTokenRequest request) {
    managePushTokenUseCase.unregister(accountId, NotificationPlatform.fromCode(request.platform()), request.token());
    return ResponseEntity.noContent().build();
  }
}
