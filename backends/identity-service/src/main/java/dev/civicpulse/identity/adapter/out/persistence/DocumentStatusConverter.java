package dev.civicpulse.identity.adapter.out.persistence;

import dev.civicpulse.identity.domain.model.DocumentStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class DocumentStatusConverter implements AttributeConverter<DocumentStatus, String> {

  @Override
  public String convertToDatabaseColumn(DocumentStatus attribute) {
    return attribute == null ? null : attribute.code();
  }

  @Override
  public DocumentStatus convertToEntityAttribute(String dbData) {
    return dbData == null ? null : DocumentStatus.fromCode(dbData);
  }
}
