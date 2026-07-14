package dev.civicpulse.payments.application.port.out;

import dev.civicpulse.payments.domain.model.PaymentMethod;
import java.util.List;
import java.util.UUID;

public interface PaymentMethodRepository {

  PaymentMethod save(PaymentMethod method);

  List<PaymentMethod> findByAccountId(UUID accountId);
}
