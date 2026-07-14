package dev.civicpulse.payments.adapter.out.persistence;

import dev.civicpulse.payments.domain.model.PaymentStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PaymentStatusConverter implements AttributeConverter<PaymentStatus, String> {

  @Override
  public String convertToDatabaseColumn(PaymentStatus attribute) {
    return attribute == null ? null : attribute.code();
  }

  @Override
  public PaymentStatus convertToEntityAttribute(String dbData) {
    return dbData == null ? null : PaymentStatus.fromCode(dbData);
  }
}
