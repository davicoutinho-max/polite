package dev.civicpulse.legislative.adapter.out.persistence;

import dev.civicpulse.legislative.domain.model.CommitteeKind;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class CommitteeKindConverter implements AttributeConverter<CommitteeKind, String> {

  @Override
  public String convertToDatabaseColumn(CommitteeKind attribute) {
    return attribute == null ? null : attribute.code();
  }

  @Override
  public CommitteeKind convertToEntityAttribute(String dbData) {
    return dbData == null ? null : CommitteeKind.fromCode(dbData);
  }
}
