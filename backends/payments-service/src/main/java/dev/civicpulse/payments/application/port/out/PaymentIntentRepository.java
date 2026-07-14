package dev.civicpulse.payments.application.port.out;

import dev.civicpulse.payments.domain.model.PaymentIntent;
import java.util.Optional;
import java.util.UUID;

public interface PaymentIntentRepository {

  PaymentIntent save(PaymentIntent intent);

  Optional<PaymentIntent> findById(UUID id);

  Optional<PaymentIntent> findByIdempotencyKey(String idempotencyKey);
}
