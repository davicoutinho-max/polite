package dev.civicpulse.messaging.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "conversations")
public class ConversationJpaEntity {

  @Id private UUID id;

  @Column(name = "is_group", nullable = false)
  private boolean group;

  @Column(name = "group_name")
  private String groupName;

  @Column(name = "group_avatar_url")
  private String groupAvatarUrl;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "last_message_at")
  private Instant lastMessageAt;

  protected ConversationJpaEntity() {}

  public ConversationJpaEntity(UUID id, boolean group, String groupName, String groupAvatarUrl, Instant createdAt, Instant lastMessageAt) {
    this.id = id;
    this.group = group;
    this.groupName = groupName;
    this.groupAvatarUrl = groupAvatarUrl;
    this.createdAt = createdAt;
    this.lastMessageAt = lastMessageAt;
  }

  public UUID getId() {
    return id;
  }

  public boolean isGroup() {
    return group;
  }

  public String getGroupName() {
    return groupName;
  }

  public String getGroupAvatarUrl() {
    return groupAvatarUrl;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getLastMessageAt() {
    return lastMessageAt;
  }
}
