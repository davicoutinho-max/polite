package dev.civicpulse.assistant.adapter.out.persistence;

import dev.civicpulse.assistant.domain.model.AssistantPromptKind;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AssistantPromptKindConverter implements AttributeConverter<AssistantPromptKind, String> {

  @Override
  public String convertToDatabaseColumn(AssistantPromptKind attribute) {
    return attribute == null ? null : attribute.code();
  }

  @Override
  public AssistantPromptKind convertToEntityAttribute(String dbData) {
    return dbData == null ? null : AssistantPromptKind.fromCode(dbData);
  }
}
