package dev.civicpulse.platformconfig.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "translation_keys")
public class TranslationKeyJpaEntity {

  @Id private UUID id;

  @Column(name = "key", nullable = false, unique = true)
  private String key;

  protected TranslationKeyJpaEntity() {}

  public TranslationKeyJpaEntity(UUID id, String key) {
    this.id = id;
    this.key = key;
  }

  public UUID getId() {
    return id;
  }

  public String getKey() {
    return key;
  }
}
