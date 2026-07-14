package dev.civicpulse.platformconfig.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "translation_values")
@IdClass(TranslationValueId.class)
public class TranslationValueJpaEntity {

  @Id
  @Column(name = "translation_key_id")
  private UUID translationKeyId;

  @Id
  @Column(name = "language_id")
  private String languageId;

  @Column(nullable = false)
  private String value;

  protected TranslationValueJpaEntity() {}

  public TranslationValueJpaEntity(UUID translationKeyId, String languageId, String value) {
    this.translationKeyId = translationKeyId;
    this.languageId = languageId;
    this.value = value;
  }

  public UUID getTranslationKeyId() {
    return translationKeyId;
  }

  public String getLanguageId() {
    return languageId;
  }

  public String getValue() {
    return value;
  }
}
