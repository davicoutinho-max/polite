package dev.civicpulse.legislative.adapter.out.persistence;

import dev.civicpulse.legislative.domain.model.VoteChoice;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class VoteChoiceConverter implements AttributeConverter<VoteChoice, String> {

  @Override
  public String convertToDatabaseColumn(VoteChoice attribute) {
    return attribute == null ? null : attribute.code();
  }

  @Override
  public VoteChoice convertToEntityAttribute(String dbData) {
    return dbData == null ? null : VoteChoice.fromCode(dbData);
  }
}
