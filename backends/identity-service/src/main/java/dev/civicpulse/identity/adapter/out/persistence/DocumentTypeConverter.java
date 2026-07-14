package dev.civicpulse.identity.adapter.out.persistence;

import dev.civicpulse.identity.domain.model.DocumentType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class DocumentTypeConverter implements AttributeConverter<DocumentType, String> {

  @Override
  public String convertToDatabaseColumn(DocumentType attribute) {
    return attribute == null ? null : attribute.code();
  }

  @Override
  public DocumentType convertToEntityAttribute(String dbData) {
    return dbData == null ? null : DocumentType.fromCode(dbData);
  }
}
