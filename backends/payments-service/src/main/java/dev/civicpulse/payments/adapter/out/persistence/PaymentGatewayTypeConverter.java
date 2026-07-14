package dev.civicpulse.payments.adapter.out.persistence;

import dev.civicpulse.payments.domain.model.PaymentGatewayType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PaymentGatewayTypeConverter implements AttributeConverter<PaymentGatewayType, String> {

  @Override
  public String convertToDatabaseColumn(PaymentGatewayType attribute) {
    return attribute == null ? null : attribute.code();
  }

  @Override
  public PaymentGatewayType convertToEntityAttribute(String dbData) {
    return dbData == null ? null : PaymentGatewayType.fromCode(dbData);
  }
}
