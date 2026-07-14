package dev.civicpulse.privacycompliance.adapter.out.persistence;

import dev.civicpulse.privacycompliance.domain.model.ExportStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ExportStatusConverter implements AttributeConverter<ExportStatus, String> {

  @Override
  public String convertToDatabaseColumn(ExportStatus attribute) {
    return attribute == null ? null : attribute.code();
  }

  @Override
  public ExportStatus convertToEntityAttribute(String dbData) {
    return dbData == null ? null : ExportStatus.fromCode(dbData);
  }
}
