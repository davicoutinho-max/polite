package dev.civicpulse.legislative.adapter.out.persistence;

import dev.civicpulse.legislative.domain.model.AccountabilityCategory;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AccountabilityCategoryConverter implements AttributeConverter<AccountabilityCategory, String> {

  @Override
  public String convertToDatabaseColumn(AccountabilityCategory attribute) {
    return attribute == null ? null : attribute.code();
  }

  @Override
  public AccountabilityCategory convertToEntityAttribute(String dbData) {
    return dbData == null ? null : AccountabilityCategory.fromCode(dbData);
  }
}
