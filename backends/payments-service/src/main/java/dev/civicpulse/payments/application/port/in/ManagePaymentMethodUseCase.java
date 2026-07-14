package dev.civicpulse.payments.application.port.in;

import dev.civicpulse.payments.domain.model.PaymentGatewayType;
import dev.civicpulse.payments.domain.model.PaymentMethod;
import java.util.List;
import java.util.UUID;

public interface ManagePaymentMethodUseCase {

  PaymentMethod registerPaymentMethod(UUID accountId, PaymentGatewayType type, String tokenRef);

  List<PaymentMethod> listByAccount(UUID accountId);
}
