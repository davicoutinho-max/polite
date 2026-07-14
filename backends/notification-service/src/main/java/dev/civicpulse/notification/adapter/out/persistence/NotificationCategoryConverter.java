package dev.civicpulse.notification.adapter.out.persistence;

import dev.civicpulse.notification.domain.model.NotificationCategory;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class NotificationCategoryConverter implements AttributeConverter<NotificationCategory, String> {

  @Override
  public String convertToDatabaseColumn(NotificationCategory attribute) {
    return attribute == null ? null : attribute.code();
  }

  @Override
  public NotificationCategory convertToEntityAttribute(String dbData) {
    return dbData == null ? null : NotificationCategory.fromCode(dbData);
  }
}
