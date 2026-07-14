package dev.civicpulse.payments.application;

import dev.civicpulse.payments.application.port.in.ManagePaymentMethodUseCase;
import dev.civicpulse.payments.application.port.out.PaymentMethodRepository;
import dev.civicpulse.payments.domain.model.PaymentGatewayType;
import dev.civicpulse.payments.domain.model.PaymentMethod;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentMethodService implements ManagePaymentMethodUseCase {

  private final PaymentMethodRepository paymentMethodRepository;
  private final Clock clock;

  public PaymentMethodService(PaymentMethodRepository paymentMethodRepository, Clock clock) {
    this.paymentMethodRepository = paymentMethodRepository;
    this.clock = clock;
  }

  @Override
  @Transactional
  public PaymentMethod registerPaymentMethod(UUID accountId, PaymentGatewayType type, String tokenRef) {
    return paymentMethodRepository.save(PaymentMethod.register(UUID.randomUUID(), accountId, type, tokenRef, clock.instant()));
  }

  @Override
  @Transactional(readOnly = true)
  public List<PaymentMethod> listByAccount(UUID accountId) {
    return paymentMethodRepository.findByAccountId(accountId);
  }
}
