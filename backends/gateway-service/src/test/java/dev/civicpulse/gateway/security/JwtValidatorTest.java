package dev.civicpulse.gateway.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Set;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;

class JwtValidatorTest {

  private static final String SIGNING_KEY_BASE64 = "u10VaF3tQ7O+0UWpsVFAZ/TYKHKEYR7CZUTDrD53RmY=";
  private static final SecretKey KEY = Keys.hmacShaKeyFor(Base64.getDecoder().decode(SIGNING_KEY_BASE64));

  private final JwtValidator validator = new JwtValidator(SIGNING_KEY_BASE64);

  @Test
  void validatesAWellFormedTokenAndExtractsClaims() {
    String token = tokenFor("acc-123", "politician", Set.of("publish-content", "react"), Duration.ofMinutes(15));

    AccountClaims claims = validator.validate(token);

    assertThat(claims.accountId()).isEqualTo("acc-123");
    assertThat(claims.accountType()).isEqualTo("politician");
    assertThat(claims.permissions()).contains("publish-content").contains("react");
  }

  @Test
  void rejectsAnExpiredToken() {
    String token = tokenFor("acc-123", "citizen", Set.of(), Duration.ofMinutes(-1));

    assertThatThrownBy(() -> validator.validate(token)).isInstanceOf(InvalidTokenException.class);
  }

  @Test
  void rejectsATokenSignedWithADifferentKey() {
    SecretKey otherKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode("dGhpcy1pcy1hLWRpZmZlcmVudC0zMi1ieXRlLWtleSEh"));
    Instant now = Instant.now();
    String token =
        Jwts.builder()
            .subject("acc-123")
            .claim("account_type", "citizen")
            .claim("permissions", Set.of())
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(Duration.ofMinutes(15))))
            .signWith(otherKey)
            .compact();

    assertThatThrownBy(() -> validator.validate(token)).isInstanceOf(InvalidTokenException.class);
  }

  @Test
  void rejectsAMalformedToken() {
    assertThatThrownBy(() -> validator.validate("not-a-real-jwt")).isInstanceOf(InvalidTokenException.class);
  }

  private static String tokenFor(String accountId, String accountType, Set<String> permissions, Duration ttl) {
    Instant now = Instant.now();
    return Jwts.builder()
        .subject(accountId)
        .claim("account_type", accountType)
        .claim("permissions", permissions)
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plus(ttl)))
        .signWith(KEY)
        .compact();
  }
}
