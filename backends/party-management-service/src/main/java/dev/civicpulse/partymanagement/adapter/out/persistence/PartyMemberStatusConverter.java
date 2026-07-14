package dev.civicpulse.partymanagement.adapter.out.persistence;

import dev.civicpulse.partymanagement.domain.model.PartyMemberStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PartyMemberStatusConverter implements AttributeConverter<PartyMemberStatus, String> {

  @Override
  public String convertToDatabaseColumn(PartyMemberStatus attribute) {
    return attribute == null ? null : attribute.code();
  }

  @Override
  public PartyMemberStatus convertToEntityAttribute(String dbData) {
    return dbData == null ? null : PartyMemberStatus.fromCode(dbData);
  }
}
