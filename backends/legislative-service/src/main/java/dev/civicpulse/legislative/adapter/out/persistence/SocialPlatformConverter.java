package dev.civicpulse.legislative.adapter.out.persistence;

import dev.civicpulse.legislative.domain.model.SocialPlatform;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class SocialPlatformConverter implements AttributeConverter<SocialPlatform, String> {

  @Override
  public String convertToDatabaseColumn(SocialPlatform attribute) {
    return attribute == null ? null : attribute.code();
  }

  @Override
  public SocialPlatform convertToEntityAttribute(String dbData) {
    return dbData == null ? null : SocialPlatform.fromCode(dbData);
  }
}
