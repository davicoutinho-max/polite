package dev.civicpulse.payments.adapter.out.persistence;

import dev.civicpulse.payments.application.port.out.PaymentMethodRepository;
import dev.civicpulse.payments.domain.model.PaymentMethod;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class PaymentMethodRepositoryAdapter implements PaymentMethodRepository {

  private final PaymentMethodJpaRepository jpaRepository;

  PaymentMethodRepositoryAdapter(PaymentMethodJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public PaymentMethod save(PaymentMethod method) {
    var saved =
        jpaRepository.save(new PaymentMethodJpaEntity(method.id(), method.accountId(), method.type(), method.tokenRef(), method.createdAt()));
    return toDomain(saved);
  }

  @Override
  public List<PaymentMethod> findByAccountId(UUID accountId) {
    return jpaRepository.findByAccountId(accountId).stream().map(PaymentMethodRepositoryAdapter::toDomain).toList();
  }

  private static PaymentMethod toDomain(PaymentMethodJpaEntity entity) {
    return PaymentMethod.reconstitute(entity.getId(), entity.getAccountId(), entity.getType(), entity.getTokenRef(), entity.getCreatedAt());
  }
}
