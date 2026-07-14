package dev.civicpulse.payments.adapter.in.web;

import dev.civicpulse.payments.adapter.in.web.dto.CreatePaymentIntentRequest;
import dev.civicpulse.payments.adapter.in.web.dto.PaymentIntentResponse;
import dev.civicpulse.payments.application.port.in.ManagePaymentIntentUseCase;
import dev.civicpulse.payments.domain.model.PaymentGatewayType;
import dev.civicpulse.payments.domain.model.PaymentPurpose;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payment-intents")
public class PaymentIntentController {

  private final ManagePaymentIntentUseCase managePaymentIntentUseCase;

  public PaymentIntentController(ManagePaymentIntentUseCase managePaymentIntentUseCase) {
    this.managePaymentIntentUseCase = managePaymentIntentUseCase;
  }

  /** {@code X-Account-Id} is the payer's own id, forwarded by the Gateway after JWT
   * validation — see docs/architecture/system-architecture.html. */
  @PostMapping
  public ResponseEntity<PaymentIntentResponse> create(
      @RequestHeader("X-Account-Id") UUID payerAccountId, @Valid @RequestBody CreatePaymentIntentRequest request) {
    var intent =
        managePaymentIntentUseCase.createAndAuthorize(
            PaymentPurpose.fromCode(request.purpose()),
            request.referenceId(),
            payerAccountId,
            request.payeeId(),
            request.amountCents(),
            PaymentGatewayType.fromCode(request.gateway()),
            request.idempotencyKey());
    PaymentIntentResponse body = PaymentIntentResponse.from(intent);
    return ResponseEntity.created(URI.create("/payment-intents/" + body.id())).body(body);
  }

  @GetMapping("/{id}")
  public PaymentIntentResponse getById(@PathVariable UUID id) {
    return PaymentIntentResponse.from(managePaymentIntentUseCase.getById(id));
  }

  @PostMapping("/{id}/capture")
  public PaymentIntentResponse capture(@PathVariable UUID id) {
    return PaymentIntentResponse.from(managePaymentIntentUseCase.capture(id));
  }

  @PostMapping("/{id}/refund")
  public PaymentIntentResponse refund(@PathVariable UUID id) {
    return PaymentIntentResponse.from(managePaymentIntentUseCase.refund(id));
  }
}
