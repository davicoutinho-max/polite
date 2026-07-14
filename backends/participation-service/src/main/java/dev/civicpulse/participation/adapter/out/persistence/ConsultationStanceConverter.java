package dev.civicpulse.participation.adapter.out.persistence;

import dev.civicpulse.participation.domain.model.ConsultationStance;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ConsultationStanceConverter implements AttributeConverter<ConsultationStance, String> {

  @Override
  public String convertToDatabaseColumn(ConsultationStance attribute) {
    return attribute == null ? null : attribute.code();
  }

  @Override
  public ConsultationStance convertToEntityAttribute(String dbData) {
    return dbData == null ? null : ConsultationStance.fromCode(dbData);
  }
}
