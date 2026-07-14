package dev.civicpulse.membershipaffiliation.adapter.out.persistence;

import dev.civicpulse.membershipaffiliation.domain.model.AffiliationStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AffiliationStatusConverter implements AttributeConverter<AffiliationStatus, String> {

  @Override
  public String convertToDatabaseColumn(AffiliationStatus attribute) {
    return attribute == null ? null : attribute.code();
  }

  @Override
  public AffiliationStatus convertToEntityAttribute(String dbData) {
    return dbData == null ? null : AffiliationStatus.fromCode(dbData);
  }
}
