package dev.civicpulse.feedcontent.adapter.out.persistence;

import dev.civicpulse.feedcontent.domain.model.TagSeverity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "post_tags")
public class PostTagJpaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "post_id", nullable = false)
  private UUID postId;

  @Column(nullable = false)
  private String label;

  private TagSeverity severity;

  private String icon;

  protected PostTagJpaEntity() {}

  public PostTagJpaEntity(Long id, UUID postId, String label, TagSeverity severity, String icon) {
    this.id = id;
    this.postId = postId;
    this.label = label;
    this.severity = severity;
    this.icon = icon;
  }

  public Long getId() {
    return id;
  }

  public UUID getPostId() {
    return postId;
  }

  public String getLabel() {
    return label;
  }

  public TagSeverity getSeverity() {
    return severity;
  }

  public String getIcon() {
    return icon;
  }
}
