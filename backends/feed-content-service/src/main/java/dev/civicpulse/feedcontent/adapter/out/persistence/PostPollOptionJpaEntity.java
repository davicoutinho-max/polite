package dev.civicpulse.feedcontent.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "post_poll_options")
public class PostPollOptionJpaEntity {

  @Id
  private UUID id;

  @Column(name = "post_id", nullable = false)
  private UUID postId;

  @Column(nullable = false)
  private String label;

  @Column(name = "sort_order", nullable = false)
  private short sortOrder;

  protected PostPollOptionJpaEntity() {}

  public PostPollOptionJpaEntity(UUID id, UUID postId, String label, short sortOrder) {
    this.id = id;
    this.postId = postId;
    this.label = label;
    this.sortOrder = sortOrder;
  }

  public UUID getId() {
    return id;
  }

  public UUID getPostId() {
    return postId;
  }

  public String getLabel() {
    return label;
  }

  public short getSortOrder() {
    return sortOrder;
  }
}
