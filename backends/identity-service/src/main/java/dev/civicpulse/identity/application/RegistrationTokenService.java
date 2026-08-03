package dev.civicpulse.identity.application;

import dev.civicpulse.identity.application.port.in.IssueRegistrationTokenUseCase;
import dev.civicpulse.identity.application.port.in.RedeemRegistrationTokenUseCase;
import dev.civicpulse.identity.application.port.out.EmailGateway;
import dev.civicpulse.identity.application.port.out.RegistrationTokenRepository;
import dev.civicpulse.identity.domain.exception.InvalidRegistrationTokenException;
import dev.civicpulse.identity.domain.exception.EmailDeliveryException;
import dev.civicpulse.identity.domain.model.AccountType;
import dev.civicpulse.identity.domain.model.RegistrationToken;
import java.security.SecureRandom;
import java.time.Clock;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationTokenService implements IssueRegistrationTokenUseCase, RedeemRegistrationTokenUseCase {

  private static final Logger log = LoggerFactory.getLogger(RegistrationTokenService.class);
  private static final SecureRandom RANDOM = new SecureRandom();

  private final RegistrationTokenRepository repository;
  private final EmailGateway emailGateway;
  private final Clock clock;

  public RegistrationTokenService(RegistrationTokenRepository repository, EmailGateway emailGateway, Clock clock) {
    this.repository = repository;
    this.emailGateway = emailGateway;
    this.clock = clock;
  }

  @Override
  @Transactional
  public RegistrationToken issue(AccountType accountType, UUID issuedByAccountId, String targetEmail, String prefillDataJson) {
    RegistrationToken token = RegistrationToken.issue(UUID.randomUUID(), generateToken(), accountType, issuedByAccountId, targetEmail, prefillDataJson, clock.instant());
    RegistrationToken saved = repository.save(token);
    sendInvite(saved);
    return saved;
  }

  @Override
  @Transactional
  public RegistrationToken resend(UUID tokenId, UUID issuedByAccountId) {
    RegistrationToken existing =
        repository.findById(tokenId).filter(t -> t.issuedByAccountId().equals(issuedByAccountId)).orElseThrow(InvalidRegistrationTokenException::new);
    existing.invalidate();
    repository.save(existing);
    return issue(existing.accountType(), existing.issuedByAccountId(), existing.targetEmail().orElse(null), existing.prefillDataJson().orElse(null));
  }

  @Override
  @Transactional(readOnly = true)
  public List<RegistrationToken> listIssuedBy(UUID issuedByAccountId) {
    return repository.findByIssuedByAccountId(issuedByAccountId);
  }

  @Override
  @Transactional(readOnly = true)
  public RegistrationToken validate(String rawToken) {
    RegistrationToken token = repository.findByToken(rawToken).orElseThrow(InvalidRegistrationTokenException::new);
    if (!token.isValid(clock.instant())) {
      throw new InvalidRegistrationTokenException();
    }
    return token;
  }

  @Override
  @Transactional
  public RegistrationToken redeem(String rawToken) {
    RegistrationToken token = repository.findByToken(rawToken).orElseThrow(InvalidRegistrationTokenException::new);
    if (!token.isValid(clock.instant())) {
      throw new InvalidRegistrationTokenException();
    }
    token.consume(clock.instant());
    return repository.save(token);
  }

  /** Best-effort — unlike participation-service's verification codes (which are useless if not
   * delivered), the issuer can see the raw token in their own dashboard and share the link
   * manually, so a delivery failure here shouldn't roll back the whole issue/resend and leave the
   * issuer with nothing at all. */
  private void sendInvite(RegistrationToken token) {
    String email = token.targetEmail().orElse(null);
    if (email == null || email.isBlank()) {
      return;
    }
    try {
      emailGateway.sendRegistrationInvite(email, token.token(), token.accountType().code());
    } catch (EmailDeliveryException e) {
      log.warn("Could not deliver registration invite email to {}: {}", email, e.getMessage());
    }
  }

  private static String generateToken() {
    byte[] bytes = new byte[24];
    RANDOM.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }
}
