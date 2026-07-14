package dev.civicpulse.payments.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface PaymentMethodJpaRepository extends JpaRepository<PaymentMethodJpaEntity, UUID> {

  List<PaymentMethodJpaEntity> findByAccountId(UUID accountId);
}
