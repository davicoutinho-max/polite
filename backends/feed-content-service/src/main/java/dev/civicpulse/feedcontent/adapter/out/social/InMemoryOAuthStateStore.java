package dev.civicpulse.feedcontent.adapter.out.social;

import dev.civicpulse.feedcontent.application.port.out.OAuthStateStore;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/** In-memory only — fine for a single-instance deployment. A multi-instance deployment behind a
 * load balancer would need this backed by something shared (e.g. Redis), since the OAuth callback
 * can land on a different instance than the one that started the flow. */
@Component
class InMemoryOAuthStateStore implements OAuthStateStore {

  private final ConcurrentHashMap<String, PendingState> states = new ConcurrentHashMap<>();

  @Override
  public void save(String state, UUID accountId, String codeVerifier) {
    states.put(state, new PendingState(accountId, codeVerifier));
  }

  @Override
  public Optional<PendingState> consume(String state) {
    return Optional.ofNullable(states.remove(state));
  }
}
