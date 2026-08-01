package dev.civicpulse.legislative.adapter.out.persistence;

import dev.civicpulse.legislative.domain.model.DisclosureStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class DisclosureStatusConverter implements AttributeConverter<DisclosureStatus, String> {

  @Override
  public String convertToDatabaseColumn(DisclosureStatus attribute) {
    return attribute == null ? null : attribute.code();
  }

  @Override
  public DisclosureStatus convertToEntityAttribute(String dbData) {
    return dbData == null ? null : DisclosureStatus.fromCode(dbData);
  }
}
