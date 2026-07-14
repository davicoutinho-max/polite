package dev.civicpulse.directory.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "follows")
@IdClass(FollowId.class)
public class FollowJpaEntity {

  @Id
  @Column(name = "follower_account_id")
  private UUID followerAccountId;

  // Stored as the raw parameter-table code (String), not the FollowTargetType enum: JPA
  // AttributeConverters are unreliable on @IdClass-participating attributes in Hibernate 6 —
  // the id-resolution path bypasses @Convert and falls back to ordinal enum mapping. Mapping
  // to/from FollowTargetType happens in FollowRepositoryAdapter instead.
  @Id
  @Column(name = "target_type")
  private String targetType;

  @Id
  @Column(name = "target_id")
  private UUID targetId;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected FollowJpaEntity() {}

  public FollowJpaEntity(UUID followerAccountId, String targetType, UUID targetId, Instant createdAt) {
    this.followerAccountId = followerAccountId;
    this.targetType = targetType;
    this.targetId = targetId;
    this.createdAt = createdAt;
  }

  public UUID getFollowerAccountId() {
    return followerAccountId;
  }

  public String getTargetType() {
    return targetType;
  }

  public UUID getTargetId() {
    return targetId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
