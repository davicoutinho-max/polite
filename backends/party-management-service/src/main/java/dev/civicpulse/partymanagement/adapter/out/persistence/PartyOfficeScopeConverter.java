package dev.civicpulse.partymanagement.adapter.out.persistence;

import dev.civicpulse.partymanagement.domain.model.PartyOfficeScope;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PartyOfficeScopeConverter implements AttributeConverter<PartyOfficeScope, String> {

  @Override
  public String convertToDatabaseColumn(PartyOfficeScope attribute) {
    return attribute == null ? null : attribute.code();
  }

  @Override
  public PartyOfficeScope convertToEntityAttribute(String dbData) {
    return dbData == null ? null : PartyOfficeScope.fromCode(dbData);
  }
}
