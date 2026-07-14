package dev.civicpulse.legislative.adapter.out.persistence;

import dev.civicpulse.legislative.domain.model.LegislativeItemStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class LegislativeItemStatusConverter implements AttributeConverter<LegislativeItemStatus, String> {

  @Override
  public String convertToDatabaseColumn(LegislativeItemStatus attribute) {
    return attribute == null ? null : attribute.code();
  }

  @Override
  public LegislativeItemStatus convertToEntityAttribute(String dbData) {
    return dbData == null ? null : LegislativeItemStatus.fromCode(dbData);
  }
}
