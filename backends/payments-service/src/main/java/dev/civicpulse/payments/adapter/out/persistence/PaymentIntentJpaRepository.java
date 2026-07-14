package dev.civicpulse.payments.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface PaymentIntentJpaRepository extends JpaRepository<PaymentIntentJpaEntity, UUID> {

  Optional<PaymentIntentJpaEntity> findByIdempotencyKey(String idempotencyKey);
}
