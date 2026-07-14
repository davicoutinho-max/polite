package dev.civicpulse.gateway.security;

/** What the gateway trusts about the caller once a JWT has been verified — mirrors the claims
 * identity-service's JwtTokenIssuerAdapter embeds (sub/account_type/permissions). */
public record AccountClaims(String accountId, String accountType, String permissions) {}
