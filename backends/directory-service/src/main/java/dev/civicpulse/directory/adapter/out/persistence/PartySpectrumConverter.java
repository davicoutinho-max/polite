package dev.civicpulse.directory.adapter.out.persistence;

import dev.civicpulse.directory.domain.model.PartySpectrum;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PartySpectrumConverter implements AttributeConverter<PartySpectrum, String> {

  @Override
  public String convertToDatabaseColumn(PartySpectrum attribute) {
    return attribute == null ? null : attribute.code();
  }

  @Override
  public PartySpectrum convertToEntityAttribute(String dbData) {
    return dbData == null ? null : PartySpectrum.fromCode(dbData);
  }
}
