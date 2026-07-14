package dev.civicpulse.identity.application.port.out;

import dev.civicpulse.identity.domain.model.AccountId;
import dev.civicpulse.identity.domain.model.Session;
import java.util.List;
import java.util.Optional;

public interface SessionRepository {

  Session save(Session session);

  Optional<Session> findByRefreshTokenHash(String refreshTokenHash);

  List<Session> findActiveByAccountId(AccountId accountId);
}
