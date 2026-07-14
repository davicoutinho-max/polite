package dev.civicpulse.identity.application;

import dev.civicpulse.identity.application.port.in.AuthResult;
import dev.civicpulse.identity.application.port.in.AuthenticateUseCase;
import dev.civicpulse.identity.application.port.out.AccountRepository;
import dev.civicpulse.identity.application.port.out.EventPublisher;
import dev.civicpulse.identity.application.port.out.PasswordHasher;
import dev.civicpulse.identity.application.port.out.RoleRepository;
import dev.civicpulse.identity.application.port.out.SessionRepository;
import dev.civicpulse.identity.application.port.out.TokenIssuer;
import dev.civicpulse.identity.domain.event.SessionIssued;
import dev.civicpulse.identity.domain.exception.InvalidCredentialsException;
import dev.civicpulse.identity.domain.model.Account;
import dev.civicpulse.identity.domain.model.Session;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticateService implements AuthenticateUseCase {

  private final AccountRepository accountRepository;
  private final SessionRepository sessionRepository;
  private final RoleRepository roleRepository;
  private final PasswordHasher passwordHasher;
  private final TokenIssuer tokenIssuer;
  private final EventPublisher eventPublisher;
  private final Clock clock;
  private final Duration refreshTokenTtl;

  public AuthenticateService(
      AccountRepository accountRepository,
      SessionRepository sessionRepository,
      RoleRepository roleRepository,
      PasswordHasher passwordHasher,
      TokenIssuer tokenIssuer,
      EventPublisher eventPublisher,
      Clock clock,
      @Value("${identity.session.refresh-token-ttl-days:30}") long refreshTokenTtlDays) {
    this.accountRepository = accountRepository;
    this.sessionRepository = sessionRepository;
    this.roleRepository = roleRepository;
    this.passwordHasher = passwordHasher;
    this.tokenIssuer = tokenIssuer;
    this.eventPublisher = eventPublisher;
    this.clock = clock;
    this.refreshTokenTtl = Duration.ofDays(refreshTokenTtlDays);
  }

  @Override
  @Transactional
  public AuthResult login(LoginCommand command) {
    Account account =
        accountRepository.findByEmail(command.email()).orElseThrow(InvalidCredentialsException::new);

    if (account.isAnonymized() || !passwordHasher.matches(command.rawPassword(), account.passwordHash())) {
      throw new InvalidCredentialsException();
    }

    Instant now = clock.instant();
    var permissions = roleRepository.findPermissionsByAccountType(account.accountType());
    var accessToken = tokenIssuer.issueAccessToken(account.id(), account.accountType(), permissions, now);

    String rawRefreshToken = tokenIssuer.generateRefreshToken();
    Session session =
        Session.issue(
            account.id(),
            tokenIssuer.hashRefreshToken(rawRefreshToken),
            command.userAgent(),
            command.ipAddress(),
            now,
            now.plus(refreshTokenTtl));
    Session saved = sessionRepository.save(session);

    eventPublisher.publish(new SessionIssued(saved.id(), account.id().value(), now));

    return new AuthResult(account.id().value(), accessToken.token(), accessToken.expiresAt(), rawRefreshToken);
  }
}
