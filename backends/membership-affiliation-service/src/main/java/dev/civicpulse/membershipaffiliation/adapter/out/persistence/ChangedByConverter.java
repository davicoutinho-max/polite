package dev.civicpulse.membershipaffiliation.adapter.out.persistence;

import dev.civicpulse.membershipaffiliation.domain.model.ChangedBy;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ChangedByConverter implements AttributeConverter<ChangedBy, String> {

  @Override
  public String convertToDatabaseColumn(ChangedBy attribute) {
    return attribute == null ? null : attribute.code();
  }

  @Override
  public ChangedBy convertToEntityAttribute(String dbData) {
    return dbData == null ? null : ChangedBy.fromCode(dbData);
  }
}
