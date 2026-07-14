package dev.civicpulse.notification.adapter.out.persistence;

import dev.civicpulse.notification.domain.model.NotificationPlatform;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "push_tokens")
@IdClass(PushTokenId.class)
public class PushTokenJpaEntity {

  @Id
  @Column(name = "account_id")
  private UUID accountId;

  /** Plain {@code String}, not the {@code NotificationPlatform} enum — {@code @Converter}
   * (autoApply) is silently ignored on {@code @Id} fields per the JPA spec (see
   * directory-service's {@code FollowJpaEntity.targetType} for the identical constraint), so
   * conversion happens manually in {@code PushTokenMapper} instead. */
  @Id
  @Column(name = "platform")
  private String platform;

  @Id
  @Column(name = "token")
  private String token;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected PushTokenJpaEntity() {}

  public PushTokenJpaEntity(UUID accountId, String platform, String token, Instant updatedAt) {
    this.accountId = accountId;
    this.platform = platform;
    this.token = token;
    this.updatedAt = updatedAt;
  }

  public UUID getAccountId() {
    return accountId;
  }

  public String getPlatform() {
    return platform;
  }

  public String getToken() {
    return token;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
