package dev.civicpulse.gateway.security;

import java.nio.charset.StandardCharsets;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * The single trust boundary for every downstream request (see
 * docs/architecture/system-architecture.html's "JWT validated once, at the edge"). Three cases:
 *
 * <ol>
 *   <li>No {@code Authorization} header — forwarded as-is (visitor). Any client-supplied
 *       {@code X-Account-Id}/{@code X-Account-Type}/{@code X-Account-Permissions} is stripped
 *       first, so a request with no valid token can never impersonate an account. Downstream
 *       services that require {@code X-Account-Id} (see each one's {@code @RequestHeader})
 *       already reject such requests on their own — no route-level public/protected allowlist
 *       is needed here for that reason alone.</li>
 *   <li>A valid {@code Bearer} token — signature+expiry verified, then {@code sub}/{@code
 *       account_type}/{@code permissions} are injected as trusted headers (after stripping any
 *       client-supplied versions of the same headers, so a valid token can't be combined with a
 *       forged extra claim).</li>
 *   <li>A present but invalid token (expired/bad signature/malformed) — always 401, even on
 *       otherwise-public routes. A bad credential is never silently downgraded to "visitor";
 *       that would hide real auth bugs from callers.</li>
 * </ol>
 *
 * <p>{@code /auth/**} and {@code /accounts/register} are exempted from validation entirely (not
 * just made optional) so an expired access token accidentally still attached to a refresh call
 * can't 401 the very call meant to replace it.
 */
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

  private static final String ACCOUNT_ID_HEADER = "X-Account-Id";
  private static final String ACCOUNT_TYPE_HEADER = "X-Account-Type";
  private static final String ACCOUNT_PERMISSIONS_HEADER = "X-Account-Permissions";

  private final JwtValidator jwtValidator;

  public JwtAuthenticationFilter(JwtValidator jwtValidator) {
    this.jwtValidator = jwtValidator;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    ServerHttpRequest request = exchange.getRequest();
    String path = request.getURI().getPath();

    if (isAlwaysPublic(path)) {
      return chain.filter(exchange);
    }

    ServerHttpRequest.Builder mutatedRequest =
        request.mutate().headers(headers -> {
          headers.remove(ACCOUNT_ID_HEADER);
          headers.remove(ACCOUNT_TYPE_HEADER);
          headers.remove(ACCOUNT_PERMISSIONS_HEADER);
        });

    String token = bearerToken(request);
    if (token == null && isWebSocketHandshake(path)) {
      // Native browser WebSocket clients cannot set custom headers on the handshake request —
      // the STOMP client falls back to a `token` query parameter for this one path only, so this
      // fallback doesn't widen the attack surface (query params linger in access logs) for every
      // other endpoint.
      token = request.getQueryParams().getFirst("token");
    }
    if (token != null) {
      AccountClaims claims;
      try {
        claims = jwtValidator.validate(token);
      } catch (InvalidTokenException e) {
        return unauthorized(exchange, e.getMessage());
      }
      mutatedRequest.headers(headers -> {
        headers.set(ACCOUNT_ID_HEADER, claims.accountId());
        headers.set(ACCOUNT_TYPE_HEADER, claims.accountType());
        headers.set(ACCOUNT_PERMISSIONS_HEADER, claims.permissions());
      });
    }

    return chain.filter(exchange.mutate().request(mutatedRequest.build()).build());
  }

  private static boolean isAlwaysPublic(String path) {
    return path.startsWith("/api/identity/auth/") || path.equals("/api/identity/accounts/register");
  }

  private static boolean isWebSocketHandshake(String path) {
    return path.equals("/api/messaging/ws") || path.startsWith("/api/messaging/ws/");
  }

  private static String bearerToken(ServerHttpRequest request) {
    String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
    if (authHeader != null && authHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
      return authHeader.substring(7).trim();
    }
    return null;
  }

  private static Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
    ServerHttpResponse response = exchange.getResponse();
    response.setStatusCode(HttpStatus.UNAUTHORIZED);
    response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
    String body = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"" + message.replace("\"", "'") + "\"}";
    DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
    return response.writeWith(Mono.just(buffer));
  }

  @Override
  public int getOrder() {
    return -1;
  }
}
