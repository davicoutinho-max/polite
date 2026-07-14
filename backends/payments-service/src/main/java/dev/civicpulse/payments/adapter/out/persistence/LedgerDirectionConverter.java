package dev.civicpulse.payments.adapter.out.persistence;

import dev.civicpulse.payments.domain.model.LedgerDirection;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class LedgerDirectionConverter implements AttributeConverter<LedgerDirection, String> {

  @Override
  public String convertToDatabaseColumn(LedgerDirection attribute) {
    return attribute == null ? null : attribute.code();
  }

  @Override
  public LedgerDirection convertToEntityAttribute(String dbData) {
    return dbData == null ? null : LedgerDirection.fromCode(dbData);
  }
}
