package dev.civicpulse.payments.adapter.in.web;

import dev.civicpulse.payments.adapter.in.web.dto.PaymentMethodResponse;
import dev.civicpulse.payments.adapter.in.web.dto.RegisterPaymentMethodRequest;
import dev.civicpulse.payments.application.port.in.ManagePaymentMethodUseCase;
import dev.civicpulse.payments.domain.model.PaymentGatewayType;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payment-methods")
public class PaymentMethodController {

  private final ManagePaymentMethodUseCase managePaymentMethodUseCase;

  public PaymentMethodController(ManagePaymentMethodUseCase managePaymentMethodUseCase) {
    this.managePaymentMethodUseCase = managePaymentMethodUseCase;
  }

  @PostMapping
  public PaymentMethodResponse register(@RequestHeader("X-Account-Id") UUID accountId, @Valid @RequestBody RegisterPaymentMethodRequest request) {
    return PaymentMethodResponse.from(
        managePaymentMethodUseCase.registerPaymentMethod(accountId, PaymentGatewayType.fromCode(request.type()), request.tokenRef()));
  }

  @GetMapping
  public List<PaymentMethodResponse> list(@RequestHeader("X-Account-Id") UUID accountId) {
    return managePaymentMethodUseCase.listByAccount(accountId).stream().map(PaymentMethodResponse::from).toList();
  }
}
