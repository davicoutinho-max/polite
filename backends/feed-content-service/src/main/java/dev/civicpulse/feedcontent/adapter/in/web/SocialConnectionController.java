package dev.civicpulse.feedcontent.adapter.in.web;

import dev.civicpulse.feedcontent.adapter.in.web.dto.SocialConnectionResponse;
import dev.civicpulse.feedcontent.adapter.in.web.dto.StartConnectResponse;
import dev.civicpulse.feedcontent.application.port.in.ManageSocialConnectionUseCase;
import dev.civicpulse.feedcontent.domain.exception.SocialOAuthException;
import dev.civicpulse.feedcontent.domain.model.SocialPlatform;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** {@code /meta/callback} and {@code /x/callback} are real OAuth redirect targets — Meta/X send
 * the citizen's own browser here directly (not an XHR from the authenticated frontend), so
 * neither can rely on {@code X-Account-Id}; both resolve the initiating account purely from the
 * {@code state} param via {@link ManageSocialConnectionUseCase}, then 302 the browser back to the
 * frontend rather than returning JSON, since there's no frontend JS running to read a JSON body
 * from a top-level navigation. */
@RestController
@RequestMapping("/social-connections")
public class SocialConnectionController {

  private final ManageSocialConnectionUseCase useCase;
  private final String frontendRedirectBaseUrl;

  public SocialConnectionController(
      ManageSocialConnectionUseCase useCase, @Value("${feed.social.frontend-redirect-base-url}") String frontendRedirectBaseUrl) {
    this.useCase = useCase;
    this.frontendRedirectBaseUrl = frontendRedirectBaseUrl;
  }

  @PostMapping("/meta/start")
  public StartConnectResponse startMeta(@RequestHeader("X-Account-Id") UUID accountId) {
    return new StartConnectResponse(useCase.startMetaConnect(accountId));
  }

  @PostMapping("/x/start")
  public StartConnectResponse startX(@RequestHeader("X-Account-Id") UUID accountId) {
    return new StartConnectResponse(useCase.startXConnect(accountId));
  }

  @GetMapping("/meta/callback")
  public ResponseEntity<Void> metaCallback(
      @RequestParam(required = false) String code, @RequestParam(required = false) String state, @RequestParam(required = false) String error) {
    if (error != null || code == null || state == null) {
      return redirectTo(errorRedirectUri("meta", error));
    }
    try {
      return redirectTo(successRedirectUri(useCase.completeMetaConnect(code, state)));
    } catch (SocialOAuthException e) {
      return redirectTo(errorRedirectUri("meta", e.getMessage()));
    }
  }

  @GetMapping("/x/callback")
  public ResponseEntity<Void> xCallback(
      @RequestParam(required = false) String code, @RequestParam(required = false) String state, @RequestParam(required = false) String error) {
    if (error != null || code == null || state == null) {
      return redirectTo(errorRedirectUri("x", error));
    }
    try {
      return redirectTo(successRedirectUri(useCase.completeXConnect(code, state)));
    } catch (SocialOAuthException e) {
      return redirectTo(errorRedirectUri("x", e.getMessage()));
    }
  }

  @GetMapping
  public List<SocialConnectionResponse> list(@RequestHeader("X-Account-Id") UUID accountId) {
    return useCase.listConnections(accountId).stream().map(SocialConnectionResponse::from).toList();
  }

  @DeleteMapping("/{platform}")
  public ResponseEntity<Void> disconnect(@RequestHeader("X-Account-Id") UUID accountId, @PathVariable String platform) {
    useCase.disconnect(accountId, SocialPlatform.fromCode(platform));
    return ResponseEntity.noContent().build();
  }

  private static ResponseEntity<Void> redirectTo(URI uri) {
    return ResponseEntity.status(HttpStatus.FOUND).location(uri).build();
  }

  private URI successRedirectUri(List<SocialPlatform> connected) {
    String platforms = connected.stream().map(SocialPlatform::code).collect(Collectors.joining(","));
    return URI.create(frontendRedirectBaseUrl + "/settings/social-connections?connected=" + platforms);
  }

  private URI errorRedirectUri(String provider, String message) {
    String reason = URLEncoder.encode(message == null ? "unknown_error" : message, StandardCharsets.UTF_8);
    return URI.create(frontendRedirectBaseUrl + "/settings/social-connections?error=" + provider + "&reason=" + reason);
  }
}
