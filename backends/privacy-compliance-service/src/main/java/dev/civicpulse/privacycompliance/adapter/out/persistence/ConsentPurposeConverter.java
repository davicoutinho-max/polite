package dev.civicpulse.privacycompliance.adapter.out.persistence;

import dev.civicpulse.privacycompliance.domain.model.ConsentPurpose;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ConsentPurposeConverter implements AttributeConverter<ConsentPurpose, String> {

  @Override
  public String convertToDatabaseColumn(ConsentPurpose attribute) {
    return attribute == null ? null : attribute.code();
  }

  @Override
  public ConsentPurpose convertToEntityAttribute(String dbData) {
    return dbData == null ? null : ConsentPurpose.fromCode(dbData);
  }
}
