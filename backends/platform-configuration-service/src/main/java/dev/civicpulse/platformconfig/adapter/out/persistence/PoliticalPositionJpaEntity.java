package dev.civicpulse.platformconfig.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "political_positions")
public class PoliticalPositionJpaEntity {

  @Id private UUID id;

  @Column(nullable = false, unique = true)
  private String name;

  @Column(name = "sort_order", nullable = false)
  private short sortOrder;

  protected PoliticalPositionJpaEntity() {}

  public PoliticalPositionJpaEntity(UUID id, String name, short sortOrder) {
    this.id = id;
    this.name = name;
    this.sortOrder = sortOrder;
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public short getSortOrder() {
    return sortOrder;
  }
}
