package dev.civicpulse.feedcontent.adapter.out.persistence;

import dev.civicpulse.feedcontent.domain.model.PostVisibility;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PostVisibilityConverter implements AttributeConverter<PostVisibility, String> {

  @Override
  public String convertToDatabaseColumn(PostVisibility attribute) {
    return attribute == null ? null : attribute.code();
  }

  @Override
  public PostVisibility convertToEntityAttribute(String dbData) {
    return dbData == null ? null : PostVisibility.fromCode(dbData);
  }
}
