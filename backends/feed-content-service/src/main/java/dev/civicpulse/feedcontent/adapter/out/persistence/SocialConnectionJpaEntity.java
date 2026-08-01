package dev.civicpulse.feedcontent.adapter.out.persistence;

import dev.civicpulse.feedcontent.domain.model.SocialPlatform;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "social_connections")
public class SocialConnectionJpaEntity {

  @Id private UUID id;

  @Column(name = "account_id", nullable = false)
  private UUID accountId;

  @Column(nullable = false)
  private SocialPlatform platform;

  @Column(name = "access_token", nullable = false)
  private String accessToken;

  @Column(name = "external_account_id", nullable = false)
  private String externalAccountId;

  @Column(name = "external_account_name")
  private String externalAccountName;

  @Column(name = "connected_at", nullable = false)
  private Instant connectedAt;

  protected SocialConnectionJpaEntity() {}

  public SocialConnectionJpaEntity(
      UUID id, UUID accountId, SocialPlatform platform, String accessToken, String externalAccountId, String externalAccountName,
      Instant connectedAt) {
    this.id = id;
    this.accountId = accountId;
    this.platform = platform;
    this.accessToken = accessToken;
    this.externalAccountId = externalAccountId;
    this.externalAccountName = externalAccountName;
    this.connectedAt = connectedAt;
  }

  public UUID getId() {
    return id;
  }

  public UUID getAccountId() {
    return accountId;
  }

  public SocialPlatform getPlatform() {
    return platform;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public String getExternalAccountId() {
    return externalAccountId;
  }

  public String getExternalAccountName() {
    return externalAccountName;
  }

  public Instant getConnectedAt() {
    return connectedAt;
  }
}
