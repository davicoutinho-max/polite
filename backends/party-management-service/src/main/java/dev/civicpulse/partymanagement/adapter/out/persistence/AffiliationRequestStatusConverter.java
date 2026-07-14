package dev.civicpulse.partymanagement.adapter.out.persistence;

import dev.civicpulse.partymanagement.domain.model.AffiliationRequestStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AffiliationRequestStatusConverter implements AttributeConverter<AffiliationRequestStatus, String> {

  @Override
  public String convertToDatabaseColumn(AffiliationRequestStatus attribute) {
    return attribute == null ? null : attribute.code();
  }

  @Override
  public AffiliationRequestStatus convertToEntityAttribute(String dbData) {
    return dbData == null ? null : AffiliationRequestStatus.fromCode(dbData);
  }
}
