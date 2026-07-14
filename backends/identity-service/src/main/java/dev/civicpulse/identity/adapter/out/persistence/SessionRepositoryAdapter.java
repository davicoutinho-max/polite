package dev.civicpulse.identity.adapter.out.persistence;

import dev.civicpulse.identity.application.port.out.SessionRepository;
import dev.civicpulse.identity.domain.model.AccountId;
import dev.civicpulse.identity.domain.model.Session;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
class SessionRepositoryAdapter implements SessionRepository {

  private final SessionJpaRepository jpaRepository;

  SessionRepositoryAdapter(SessionJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public Session save(Session session) {
    SessionJpaEntity saved =
        jpaRepository.save(
            new SessionJpaEntity(
                session.id(),
                session.accountId().value(),
                session.refreshTokenHash(),
                session.userAgent().orElse(null),
                session.ipAddress().orElse(null),
                session.issuedAt(),
                session.expiresAt(),
                session.revokedAt().orElse(null)));
    return toDomain(saved);
  }

  @Override
  public Optional<Session> findByRefreshTokenHash(String refreshTokenHash) {
    return jpaRepository.findByRefreshTokenHash(refreshTokenHash).map(SessionRepositoryAdapter::toDomain);
  }

  @Override
  public List<Session> findActiveByAccountId(AccountId accountId) {
    return jpaRepository.findActiveByAccountId(accountId.value(), Instant.now()).stream()
        .map(SessionRepositoryAdapter::toDomain)
        .toList();
  }

  private static Session toDomain(SessionJpaEntity entity) {
    return Session.reconstitute(
        entity.getId(),
        AccountId.of(entity.getAccountId()),
        entity.getRefreshTokenHash(),
        entity.getUserAgent(),
        entity.getIpAddress(),
        entity.getIssuedAt(),
        entity.getExpiresAt(),
        entity.getRevokedAt());
  }
}
