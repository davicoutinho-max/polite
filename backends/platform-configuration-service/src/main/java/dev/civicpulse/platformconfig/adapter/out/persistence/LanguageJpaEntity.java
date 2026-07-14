package dev.civicpulse.platformconfig.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "languages")
public class LanguageJpaEntity {

  @Id private String id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String code;

  @Column(name = "is_default", nullable = false)
  private boolean isDefault;

  protected LanguageJpaEntity() {}

  public LanguageJpaEntity(String id, String name, String code, boolean isDefault) {
    this.id = id;
    this.name = name;
    this.code = code;
    this.isDefault = isDefault;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getCode() {
    return code;
  }

  public boolean isDefault() {
    return isDefault;
  }
}
