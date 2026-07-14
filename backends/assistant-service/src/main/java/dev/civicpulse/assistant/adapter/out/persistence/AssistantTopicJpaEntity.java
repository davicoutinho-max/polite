package dev.civicpulse.assistant.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "assistant_topics")
public class AssistantTopicJpaEntity {

  @Id
  @GeneratedValue
  private UUID id;

  @Column(nullable = false)
  private String reference;

  @Column(nullable = false)
  private String title;

  @Column(name = "legislative_item_id")
  private UUID legislativeItemId;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected AssistantTopicJpaEntity() {}

  public AssistantTopicJpaEntity(UUID id, String reference, String title, UUID legislativeItemId, Instant createdAt) {
    this.id = id;
    this.reference = reference;
    this.title = title;
    this.legislativeItemId = legislativeItemId;
    this.createdAt = createdAt;
  }

  public UUID getId() {
    return id;
  }

  public String getReference() {
    return reference;
  }

  public String getTitle() {
    return title;
  }

  public UUID getLegislativeItemId() {
    return legislativeItemId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
