package dev.civicpulse.legislative.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "team_members")
public class TeamMemberJpaEntity {

  @Id
  @GeneratedValue
  private UUID id;

  @Column(name = "politician_account_id", nullable = false)
  private UUID politicianAccountId;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String role;

  @Column(name = "avatar_url")
  private String avatarUrl;

  protected TeamMemberJpaEntity() {}

  public TeamMemberJpaEntity(UUID id, UUID politicianAccountId, String name, String role, String avatarUrl) {
    this.id = id;
    this.politicianAccountId = politicianAccountId;
    this.name = name;
    this.role = role;
    this.avatarUrl = avatarUrl;
  }

  public UUID getId() {
    return id;
  }

  public UUID getPoliticianAccountId() {
    return politicianAccountId;
  }

  public String getName() {
    return name;
  }

  public String getRole() {
    return role;
  }

  public String getAvatarUrl() {
    return avatarUrl;
  }
}
