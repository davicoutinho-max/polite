package dev.civicpulse.analytics.adapter.out.persistence;

import dev.civicpulse.analytics.domain.model.EngagementEventType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EngagementEventTypeConverter implements AttributeConverter<EngagementEventType, String> {

  @Override
  public String convertToDatabaseColumn(EngagementEventType attribute) {
    return attribute == null ? null : attribute.code();
  }

  @Override
  public EngagementEventType convertToEntityAttribute(String dbData) {
    return dbData == null ? null : EngagementEventType.fromCode(dbData);
  }
}
