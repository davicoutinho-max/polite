package dev.civicpulse.directory.adapter.out.persistence;

import dev.civicpulse.directory.domain.model.GovLevel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcType;

@Entity
@Table(name = "politicians")
public class PoliticianJpaEntity {

  @Id
  @Column(name = "account_id")
  private UUID accountId;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false, unique = true)
  @JdbcType(CitextJdbcType.class)
  private String handle;

  @Column(name = "avatar_url")
  private String avatarUrl;

  @Column(nullable = false)
  private boolean verified;

  private String office;

  private GovLevel level;

  @Column(name = "party_id")
  private UUID partyId;

  @Column(name = "party_acronym")
  private String partyAcronym;

  private String state;

  @Column(name = "followers_count", nullable = false)
  private int followersCount;

  @Column(name = "bills_count", nullable = false)
  private int billsCount;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected PoliticianJpaEntity() {}

  public PoliticianJpaEntity(
      UUID accountId,
      String name,
      String handle,
      String avatarUrl,
      boolean verified,
      String office,
      GovLevel level,
      UUID partyId,
      String partyAcronym,
      String state,
      int followersCount,
      int billsCount,
      Instant updatedAt) {
    this.accountId = accountId;
    this.name = name;
    this.handle = handle;
    this.avatarUrl = avatarUrl;
    this.verified = verified;
    this.office = office;
    this.level = level;
    this.partyId = partyId;
    this.partyAcronym = partyAcronym;
    this.state = state;
    this.followersCount = followersCount;
    this.billsCount = billsCount;
    this.updatedAt = updatedAt;
  }

  public UUID getAccountId() {
    return accountId;
  }

  public String getName() {
    return name;
  }

  public String getHandle() {
    return handle;
  }

  public String getAvatarUrl() {
    return avatarUrl;
  }

  public boolean isVerified() {
    return verified;
  }

  public String getOffice() {
    return office;
  }

  public GovLevel getLevel() {
    return level;
  }

  public UUID getPartyId() {
    return partyId;
  }

  public String getPartyAcronym() {
    return partyAcronym;
  }

  public String getState() {
    return state;
  }

  public int getFollowersCount() {
    return followersCount;
  }

  public int getBillsCount() {
    return billsCount;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
