package dev.civicpulse.payments.adapter.out.persistence;

import dev.civicpulse.payments.application.port.out.PaymentIntentRepository;
import dev.civicpulse.payments.domain.model.PaymentIntent;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class PaymentIntentRepositoryAdapter implements PaymentIntentRepository {

  private final PaymentIntentJpaRepository jpaRepository;
  private final PaymentIntentMapper mapper;

  PaymentIntentRepositoryAdapter(PaymentIntentJpaRepository jpaRepository, PaymentIntentMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public PaymentIntent save(PaymentIntent intent) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(intent)));
  }

  @Override
  public Optional<PaymentIntent> findById(UUID id) {
    return jpaRepository.findById(id).map(mapper::toDomain);
  }

  @Override
  public Optional<PaymentIntent> findByIdempotencyKey(String idempotencyKey) {
    return jpaRepository.findByIdempotencyKey(idempotencyKey).map(mapper::toDomain);
  }
}
