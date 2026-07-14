package dev.civicpulse.identity.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "sessions")
public class SessionJpaEntity {

  @Id private UUID id;

  @Column(name = "account_id", nullable = false)
  private UUID accountId;

  @Column(name = "refresh_token_hash", nullable = false, unique = true)
  private String refreshTokenHash;

  @Column(name = "user_agent")
  private String userAgent;

  @Column(name = "ip_address")
  @JdbcTypeCode(SqlTypes.INET)
  private String ipAddress;

  @Column(name = "issued_at", nullable = false)
  private Instant issuedAt;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Column(name = "revoked_at")
  private Instant revokedAt;

  protected SessionJpaEntity() {}

  public SessionJpaEntity(
      UUID id,
      UUID accountId,
      String refreshTokenHash,
      String userAgent,
      String ipAddress,
      Instant issuedAt,
      Instant expiresAt,
      Instant revokedAt) {
    this.id = id;
    this.accountId = accountId;
    this.refreshTokenHash = refreshTokenHash;
    this.userAgent = userAgent;
    this.ipAddress = ipAddress;
    this.issuedAt = issuedAt;
    this.expiresAt = expiresAt;
    this.revokedAt = revokedAt;
  }

  public UUID getId() {
    return id;
  }

  public UUID getAccountId() {
    return accountId;
  }

  public String getRefreshTokenHash() {
    return refreshTokenHash;
  }

  public String getUserAgent() {
    return userAgent;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public Instant getIssuedAt() {
    return issuedAt;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }

  public Instant getRevokedAt() {
    return revokedAt;
  }
}
