package dev.civicpulse.feedcontent.application.port.out;

import java.util.Optional;
import java.util.UUID;

/** Bridges an OAuth "authorize" call to its later "callback" — the state param round-trips
 * through the external provider's own redirect, so this is how the callback learns which
 * account started the flow (and, for X's PKCE flow, the code verifier it must replay). One-time
 * use: {@link #consume} removes the entry, so a replayed callback can't reuse a stale state. */
public interface OAuthStateStore {

  void save(String state, UUID accountId, String codeVerifier);

  Optional<PendingState> consume(String state);

  record PendingState(UUID accountId, String codeVerifier) {}
}
