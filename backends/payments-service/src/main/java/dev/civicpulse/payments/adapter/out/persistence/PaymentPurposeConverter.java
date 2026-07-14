package dev.civicpulse.payments.adapter.out.persistence;

import dev.civicpulse.payments.domain.model.PaymentPurpose;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PaymentPurposeConverter implements AttributeConverter<PaymentPurpose, String> {

  @Override
  public String convertToDatabaseColumn(PaymentPurpose attribute) {
    return attribute == null ? null : attribute.code();
  }

  @Override
  public PaymentPurpose convertToEntityAttribute(String dbData) {
    return dbData == null ? null : PaymentPurpose.fromCode(dbData);
  }
}
