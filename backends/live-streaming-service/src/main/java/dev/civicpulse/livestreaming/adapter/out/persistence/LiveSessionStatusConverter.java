package dev.civicpulse.livestreaming.adapter.out.persistence;

import dev.civicpulse.livestreaming.domain.model.LiveSessionStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class LiveSessionStatusConverter implements AttributeConverter<LiveSessionStatus, String> {

  @Override
  public String convertToDatabaseColumn(LiveSessionStatus attribute) {
    return attribute == null ? null : attribute.code();
  }

  @Override
  public LiveSessionStatus convertToEntityAttribute(String dbData) {
    return dbData == null ? null : LiveSessionStatus.fromCode(dbData);
  }
}
