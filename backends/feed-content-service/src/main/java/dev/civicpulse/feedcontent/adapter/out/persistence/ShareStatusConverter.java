package dev.civicpulse.feedcontent.adapter.out.persistence;

import dev.civicpulse.feedcontent.domain.model.ShareStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ShareStatusConverter implements AttributeConverter<ShareStatus, String> {

  @Override
  public String convertToDatabaseColumn(ShareStatus attribute) {
    return attribute == null ? null : attribute.code();
  }

  @Override
  public ShareStatus convertToEntityAttribute(String dbData) {
    return dbData == null ? null : ShareStatus.fromCode(dbData);
  }
}
