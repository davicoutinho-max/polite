package dev.civicpulse.fundraising.adapter.out.persistence;

import dev.civicpulse.fundraising.domain.model.FundraiserCategory;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class FundraiserCategoryConverter implements AttributeConverter<FundraiserCategory, String> {

  @Override
  public String convertToDatabaseColumn(FundraiserCategory attribute) {
    return attribute == null ? null : attribute.code();
  }

  @Override
  public FundraiserCategory convertToEntityAttribute(String dbData) {
    return dbData == null ? null : FundraiserCategory.fromCode(dbData);
  }
}
