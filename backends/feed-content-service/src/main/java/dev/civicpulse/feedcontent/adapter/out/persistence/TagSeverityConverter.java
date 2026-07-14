package dev.civicpulse.feedcontent.adapter.out.persistence;

import dev.civicpulse.feedcontent.domain.model.TagSeverity;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TagSeverityConverter implements AttributeConverter<TagSeverity, String> {

  @Override
  public String convertToDatabaseColumn(TagSeverity attribute) {
    return attribute == null ? null : attribute.code();
  }

  @Override
  public TagSeverity convertToEntityAttribute(String dbData) {
    return dbData == null ? null : TagSeverity.fromCode(dbData);
  }
}
