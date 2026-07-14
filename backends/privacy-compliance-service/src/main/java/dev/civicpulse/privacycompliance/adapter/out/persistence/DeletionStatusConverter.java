package dev.civicpulse.privacycompliance.adapter.out.persistence;

import dev.civicpulse.privacycompliance.domain.model.DeletionStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class DeletionStatusConverter implements AttributeConverter<DeletionStatus, String> {

  @Override
  public String convertToDatabaseColumn(DeletionStatus attribute) {
    return attribute == null ? null : attribute.code();
  }

  @Override
  public DeletionStatus convertToEntityAttribute(String dbData) {
    return dbData == null ? null : DeletionStatus.fromCode(dbData);
  }
}
