package dev.civicpulse.activityfeed.adapter.out.persistence;

import dev.civicpulse.activityfeed.domain.model.TimelineEventType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TimelineEventTypeConverter implements AttributeConverter<TimelineEventType, String> {

  @Override
  public String convertToDatabaseColumn(TimelineEventType attribute) {
    return attribute == null ? null : attribute.code();
  }

  @Override
  public TimelineEventType convertToEntityAttribute(String dbData) {
    return dbData == null ? null : TimelineEventType.fromCode(dbData);
  }
}
