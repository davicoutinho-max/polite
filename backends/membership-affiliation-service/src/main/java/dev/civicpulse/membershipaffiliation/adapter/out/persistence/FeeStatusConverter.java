package dev.civicpulse.membershipaffiliation.adapter.out.persistence;

import dev.civicpulse.membershipaffiliation.domain.model.FeeStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class FeeStatusConverter implements AttributeConverter<FeeStatus, String> {

  @Override
  public String convertToDatabaseColumn(FeeStatus attribute) {
    return attribute == null ? null : attribute.code();
  }

  @Override
  public FeeStatus convertToEntityAttribute(String dbData) {
    return dbData == null ? null : FeeStatus.fromCode(dbData);
  }
}
