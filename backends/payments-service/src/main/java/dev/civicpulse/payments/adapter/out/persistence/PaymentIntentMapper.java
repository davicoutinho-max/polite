package dev.civicpulse.payments.adapter.out.persistence;

import dev.civicpulse.payments.domain.model.PaymentIntent;
import org.springframework.stereotype.Component;

@Component
class PaymentIntentMapper {

  PaymentIntent toDomain(PaymentIntentJpaEntity entity) {
    return PaymentIntent.reconstitute(
        entity.getId(),
        entity.getPurpose(),
        entity.getReferenceId(),
        entity.getPayerAccountId(),
        entity.getPayeeId(),
        entity.getAmountCents(),
        entity.getCurrency(),
        entity.getStatus(),
        entity.getGateway(),
        entity.getGatewayRef(),
        entity.getIdempotencyKey(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }

  PaymentIntentJpaEntity toEntity(PaymentIntent intent) {
    return new PaymentIntentJpaEntity(
        intent.id(),
        intent.purpose(),
        intent.referenceId(),
        intent.payerAccountId(),
        intent.payeeId(),
        intent.amountCents(),
        intent.currency(),
        intent.status(),
        intent.gateway(),
        intent.gatewayRef().orElse(null),
        intent.idempotencyKey(),
        intent.createdAt(),
        intent.updatedAt());
  }
}
