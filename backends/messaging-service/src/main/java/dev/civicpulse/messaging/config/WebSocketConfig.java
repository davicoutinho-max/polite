package dev.civicpulse.messaging.config;

import java.security.Principal;
import java.util.Map;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

/** Live message push, typing indicators and read receipts over STOMP/WebSocket — REST stays the
 * source of truth (send/edit/delete/markRead all persist first), this is purely a fan-out layer
 * on top so every open tab sees the same state without polling. */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  private static final String ACCOUNT_ID_ATTRIBUTE = "accountId";

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.enableSimpleBroker("/topic");
    registry.setApplicationDestinationPrefixes("/app");
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry
        .addEndpoint("/ws")
        .setAllowedOriginPatterns("*")
        .addInterceptors(new AccountIdHandshakeInterceptor())
        .setHandshakeHandler(new AccountIdHandshakeHandler());
  }

  /** Reads the {@code X-Account-Id} header the gateway already validated and stashes it in the
   * WebSocket session's attributes, for AccountIdHandshakeHandler to pick up right after. Missing
   * entirely for an unauthenticated (visitor) handshake — the connection still succeeds (matching
   * this app's "visitor" concept elsewhere), it just never gets a Principal, so per-user framing
   * (@MessageMapping's Principal argument) is unavailable to it. */
  private static final class AccountIdHandshakeInterceptor implements HandshakeInterceptor {
    @Override
    public boolean beforeHandshake(
        ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) {
      if (request instanceof ServletServerHttpRequest servletRequest) {
        String accountId = servletRequest.getServletRequest().getHeader("X-Account-Id");
        if (accountId != null && !accountId.isBlank()) {
          attributes.put(ACCOUNT_ID_ATTRIBUTE, accountId);
        }
      }
      return true;
    }

    @Override
    public void afterHandshake(
        ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {}
  }

  private static final class AccountIdHandshakeHandler extends DefaultHandshakeHandler {
    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
      Object accountId = attributes.get(ACCOUNT_ID_ATTRIBUTE);
      return accountId != null ? new StompPrincipal(accountId.toString()) : super.determineUser(request, wsHandler, attributes);
    }
  }
}
