package dev.civicpulse.feedcontent.adapter.out.persistence;

import dev.civicpulse.feedcontent.domain.model.PostKind;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PostKindConverter implements AttributeConverter<PostKind, String> {

  @Override
  public String convertToDatabaseColumn(PostKind attribute) {
    return attribute == null ? null : attribute.code();
  }

  @Override
  public PostKind convertToEntityAttribute(String dbData) {
    return dbData == null ? null : PostKind.fromCode(dbData);
  }
}
