package dev.civicpulse.legislative.adapter.out.persistence;

import dev.civicpulse.legislative.domain.model.SocialPlatform;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "social_links")
public class SocialLinkJpaEntity {

  @Id
  @GeneratedValue
  private UUID id;

  @Column(name = "politician_account_id", nullable = false)
  private UUID politicianAccountId;

  @Column(nullable = false)
  private SocialPlatform platform;

  @Column(nullable = false)
  private String label;

  @Column(nullable = false)
  private String handle;

  @Column(nullable = false)
  private String url;

  protected SocialLinkJpaEntity() {}

  public SocialLinkJpaEntity(UUID id, UUID politicianAccountId, SocialPlatform platform, String label, String handle, String url) {
    this.id = id;
    this.politicianAccountId = politicianAccountId;
    this.platform = platform;
    this.label = label;
    this.handle = handle;
    this.url = url;
  }

  public UUID getId() {
    return id;
  }

  public UUID getPoliticianAccountId() {
    return politicianAccountId;
  }

  public SocialPlatform getPlatform() {
    return platform;
  }

  public String getLabel() {
    return label;
  }

  public String getHandle() {
    return handle;
  }

  public String getUrl() {
    return url;
  }
}
