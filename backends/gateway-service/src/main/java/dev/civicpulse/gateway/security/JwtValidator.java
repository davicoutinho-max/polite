package dev.civicpulse.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Base64;
import java.util.Collection;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;

/** Validates JWTs minted by identity-service's {@code JwtTokenIssuerAdapter} — same HMAC key,
 * same claim names ({@code sub}/{@code account_type}/{@code permissions}). The gateway only
 * checks signature and expiry; it never re-derives permissions logic, it just forwards the
 * claims downstream as trusted headers (see docs/architecture/system-architecture.html's "JWT
 * validated once, at the edge"). No framework/reactive types here on purpose — this class is
 * pure and unit-testable in isolation from the WebFlux filter that calls it. */
public class JwtValidator {

  private final SecretKey signingKey;

  public JwtValidator(String signingKeyBase64) {
    this.signingKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(signingKeyBase64));
  }

  public AccountClaims validate(String token) {
    try {
      Claims claims = Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
      String accountId = claims.getSubject();
      String accountType = claims.get("account_type", String.class);
      String permissions = joinPermissions(claims.get("permissions"));
      return new AccountClaims(accountId, accountType, permissions);
    } catch (JwtException | IllegalArgumentException e) {
      throw new InvalidTokenException("Token failed validation: " + e.getMessage(), e);
    }
  }

  private static String joinPermissions(Object permissionsClaim) {
    if (permissionsClaim instanceof Collection<?> collection) {
      return collection.stream().map(String::valueOf).collect(Collectors.joining(","));
    }
    return "";
  }
}
