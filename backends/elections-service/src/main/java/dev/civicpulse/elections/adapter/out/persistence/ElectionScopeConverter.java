package dev.civicpulse.elections.adapter.out.persistence;

import dev.civicpulse.elections.domain.model.ElectionScope;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ElectionScopeConverter implements AttributeConverter<ElectionScope, String> {

  @Override
  public String convertToDatabaseColumn(ElectionScope attribute) {
    return attribute == null ? null : attribute.code();
  }

  @Override
  public ElectionScope convertToEntityAttribute(String dbData) {
    return dbData == null ? null : ElectionScope.fromCode(dbData);
  }
}
