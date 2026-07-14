package dev.civicpulse.identity.adapter.out.persistence;

import dev.civicpulse.identity.domain.model.AccountType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AccountTypeConverter implements AttributeConverter<AccountType, String> {

  @Override
  public String convertToDatabaseColumn(AccountType attribute) {
    return attribute == null ? null : attribute.code();
  }

  @Override
  public AccountType convertToEntityAttribute(String dbData) {
    return dbData == null ? null : AccountType.fromCode(dbData);
  }
}
