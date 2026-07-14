package dev.civicpulse.legislative.adapter.out.persistence;

import dev.civicpulse.legislative.domain.model.LegislativeItemCategory;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class LegislativeItemCategoryConverter implements AttributeConverter<LegislativeItemCategory, String> {

  @Override
  public String convertToDatabaseColumn(LegislativeItemCategory attribute) {
    return attribute == null ? null : attribute.code();
  }

  @Override
  public LegislativeItemCategory convertToEntityAttribute(String dbData) {
    return dbData == null ? null : LegislativeItemCategory.fromCode(dbData);
  }
}
