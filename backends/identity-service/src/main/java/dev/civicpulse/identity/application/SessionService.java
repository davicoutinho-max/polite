package dev.civicpulse.identity.application;

import dev.civicpulse.identity.application.port.in.AuthResult;
import dev.civicpulse.identity.application.port.in.ManageSessionUseCase;
import dev.civicpulse.identity.application.port.out.AccountRepository;
import dev.civicpulse.identity.application.port.out.EventPublisher;
import dev.civicpulse.identity.application.port.out.RoleRepository;
import dev.civicpulse.identity.application.port.out.SessionRepository;
import dev.civicpulse.identity.application.port.out.TokenIssuer;
import dev.civicpulse.identity.domain.event.SessionIssued;
import dev.civicpulse.identity.domain.event.SessionRevoked;
import dev.civicpulse.identity.domain.exception.AccountNotFoundException;
import dev.civicpulse.identity.domain.exception.SessionNotActiveException;
import dev.civicpulse.identity.domain.model.Account;
import dev.civicpulse.identity.domain.model.AccountId;
import dev.civicpulse.identity.domain.model.Session;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SessionService implements ManageSessionUseCase {

  private final SessionRepository sessionRepository;
  private final AccountRepository accountRepository;
  private final RoleRepository roleRepository;
  private final TokenIssuer tokenIssuer;
  private final EventPublisher eventPublisher;
  private final Clock clock;
  private final Duration refreshTokenTtl;

  public SessionService(
      SessionRepository sessionRepository,
      AccountRepository accountRepository,
      RoleRepository roleRepository,
      TokenIssuer tokenIssuer,
      EventPublisher eventPublisher,
      Clock clock,
      @Value("${identity.session.refresh-token-ttl-days:30}") long refreshTokenTtlDays) {
    this.sessionRepository = sessionRepository;
    this.accountRepository = accountRepository;
    this.roleRepository = roleRepository;
    this.tokenIssuer = tokenIssuer;
    this.eventPublisher = eventPublisher;
    this.clock = clock;
    this.refreshTokenTtl = Duration.ofDays(refreshTokenTtlDays);
  }

  @Override
  @Transactional
  public AuthResult refresh(String rawRefreshToken, String userAgent, String ipAddress) {
    String hash = tokenIssuer.hashRefreshToken(rawRefreshToken);
    Session existing = sessionRepository.findByRefreshTokenHash(hash).orElseThrow(SessionNotActiveException::new);

    Instant now = clock.instant();
    if (!existing.isActive(now)) {
      throw new SessionNotActiveException();
    }

    Account account =
        accountRepository.findById(existing.accountId()).orElseThrow(() -> new AccountNotFoundException(existing.accountId().toString()));

    // Rotate the refresh token on every use — a stolen, already-used token becomes worthless.
    existing.revoke(now);
    sessionRepository.save(existing);

    var permissions = roleRepository.findPermissionsByAccountType(account.accountType());
    var accessToken = tokenIssuer.issueAccessToken(account.id(), account.accountType(), permissions, now);
    String newRawRefreshToken = tokenIssuer.generateRefreshToken();
    Session rotated =
        Session.issue(
            account.id(), tokenIssuer.hashRefreshToken(newRawRefreshToken), userAgent, ipAddress, now, now.plus(refreshTokenTtl));
    Session saved = sessionRepository.save(rotated);

    eventPublisher.publish(new SessionIssued(saved.id(), account.id().value(), now));

    return new AuthResult(account.id().value(), accessToken.token(), accessToken.expiresAt(), newRawRefreshToken);
  }

  @Override
  @Transactional
  public void revoke(String rawRefreshToken) {
    String hash = tokenIssuer.hashRefreshToken(rawRefreshToken);
    sessionRepository
        .findByRefreshTokenHash(hash)
        .ifPresent(
            session -> {
              Instant now = clock.instant();
              session.revoke(now);
              sessionRepository.save(session);
              eventPublisher.publish(new SessionRevoked(session.id(), now));
            });
  }

  @Override
  @Transactional
  public void revokeAll(AccountId accountId) {
    Instant now = clock.instant();
    for (Session session : sessionRepository.findActiveByAccountId(accountId)) {
      session.revoke(now);
      sessionRepository.save(session);
      eventPublisher.publish(new SessionRevoked(session.id(), now));
    }
  }
}
