package dev.civicpulse.messaging.config;

import java.security.Principal;

/** Wraps the account id the gateway already validated (forwarded as {@code X-Account-Id} on the
 * WebSocket handshake) — see WebSocketConfig's HandshakeInterceptor for where this gets set. No
 * JWT parsing happens here; the gateway remains the single trust boundary, same as every REST
 * endpoint in this service that trusts the header outright. */
public record StompPrincipal(String name) implements Principal {

  @Override
  public String getName() {
    return name;
  }
}
