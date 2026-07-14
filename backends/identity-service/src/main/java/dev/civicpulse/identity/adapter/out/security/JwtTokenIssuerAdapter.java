package dev.civicpulse.identity.adapter.out.security;

import dev.civicpulse.identity.application.port.out.TokenIssuer;
import dev.civicpulse.identity.domain.model.AccountId;
import dev.civicpulse.identity.domain.model.AccountType;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Set;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Mints the JWT the API Gateway validates on every request (see "JWT validated once, at the
 * edge" in docs/architecture/system-architecture.html). The Gateway only checks the signature
 * and expiry — the permission claims embedded here are what every downstream service then
 * re-asserts independently against its own route.
 */
@Component
public class JwtTokenIssuerAdapter implements TokenIssuer {

  private final SecretKey signingKey;
  private final Duration accessTokenTtl;
  private final SecureRandom secureRandom = new SecureRandom();

  public JwtTokenIssuerAdapter(
      @Value("${identity.jwt.signing-key-base64}") String signingKeyBase64,
      @Value("${identity.session.access-token-ttl-minutes:15}") long accessTokenTtlMinutes) {
    this.signingKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(signingKeyBase64));
    this.accessTokenTtl = Duration.ofMinutes(accessTokenTtlMinutes);
  }

  @Override
  public IssuedToken issueAccessToken(AccountId accountId, AccountType accountType, Set<String> permissions, Instant now) {
    Instant expiresAt = now.plus(accessTokenTtl);
    String token =
        Jwts.builder()
            .subject(accountId.value().toString())
            .claim("account_type", accountType.code())
            .claim("permissions", permissions)
            .issuedAt(java.util.Date.from(now))
            .expiration(java.util.Date.from(expiresAt))
            .signWith(signingKey)
            .compact();
    return new IssuedToken(token, expiresAt);
  }

  @Override
  public String generateRefreshToken() {
    byte[] bytes = new byte[32];
    secureRandom.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  @Override
  public String hashRefreshToken(String rawRefreshToken) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return Base64.getEncoder().encodeToString(digest.digest(rawRefreshToken.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }
}
