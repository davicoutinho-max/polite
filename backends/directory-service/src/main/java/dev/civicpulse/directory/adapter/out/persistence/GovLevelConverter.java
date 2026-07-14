package dev.civicpulse.directory.adapter.out.persistence;

import dev.civicpulse.directory.domain.model.GovLevel;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class GovLevelConverter implements AttributeConverter<GovLevel, String> {

  @Override
  public String convertToDatabaseColumn(GovLevel attribute) {
    return attribute == null ? null : attribute.code();
  }

  @Override
  public GovLevel convertToEntityAttribute(String dbData) {
    return dbData == null ? null : GovLevel.fromCode(dbData);
  }
}
