package dev.civicpulse.payments.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.civicpulse.payments.application.port.in.ManagePaymentIntentUseCase;
import dev.civicpulse.payments.application.port.out.CheckoutGateway;
import dev.civicpulse.payments.application.port.out.LedgerEntryRepository;
import dev.civicpulse.payments.application.port.out.OutboxEventRepository;
import dev.civicpulse.payments.application.port.out.PayerLookupGateway;
import dev.civicpulse.payments.application.port.out.PaymentIntentRepository;
import dev.civicpulse.payments.domain.event.DomainEvent;
import dev.civicpulse.payments.domain.event.PaymentAuthorized;
import dev.civicpulse.payments.domain.event.PaymentCaptured;
import dev.civicpulse.payments.domain.event.PaymentRefunded;
import dev.civicpulse.payments.domain.exception.PaymentIntentNotFoundException;
import dev.civicpulse.payments.domain.model.LedgerDirection;
import dev.civicpulse.payments.domain.model.LedgerEntry;
import dev.civicpulse.payments.domain.model.OutboxEvent;
import dev.civicpulse.payments.domain.model.PaymentGatewayType;
import dev.civicpulse.payments.domain.model.PaymentIntent;
import dev.civicpulse.payments.domain.model.PaymentPurpose;
import dev.civicpulse.payments.domain.model.PaymentStatus;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentIntentService implements ManagePaymentIntentUseCase {

  private final PaymentIntentRepository paymentIntentRepository;
  private final LedgerEntryRepository ledgerEntryRepository;
  private final OutboxEventRepository outboxEventRepository;
  private final PayerLookupGateway payerLookupGateway;
  private final CheckoutGateway checkoutGateway;
  private final ObjectMapper objectMapper;
  private final Clock clock;

  public PaymentIntentService(
      PaymentIntentRepository paymentIntentRepository,
      LedgerEntryRepository ledgerEntryRepository,
      OutboxEventRepository outboxEventRepository,
      PayerLookupGateway payerLookupGateway,
      CheckoutGateway checkoutGateway,
      ObjectMapper objectMapper,
      Clock clock) {
    this.paymentIntentRepository = paymentIntentRepository;
    this.ledgerEntryRepository = ledgerEntryRepository;
    this.outboxEventRepository = outboxEventRepository;
    this.payerLookupGateway = payerLookupGateway;
    this.checkoutGateway = checkoutGateway;
    this.objectMapper = objectMapper;
    this.clock = clock;
  }

  @Override
  @Transactional
  public PaymentIntent createPendingPayment(
      PaymentPurpose purpose, UUID referenceId, UUID payerAccountId, UUID payeeId, long amountCents, PaymentGatewayType gateway, String idempotencyKey) {
    var existing = paymentIntentRepository.findByIdempotencyKey(idempotencyKey);
    if (existing.isPresent()) {
      return existing.get();
    }
    Instant now = clock.instant();
    PaymentIntent intent =
        PaymentIntent.create(UUID.randomUUID(), purpose, referenceId, payerAccountId, payeeId, amountCents, "BRL", gateway, idempotencyKey, now);
    paymentIntentRepository.save(intent);
    return intent;
  }

  @Override
  @Transactional(readOnly = true)
  public String createCheckoutUrl(UUID intentId) {
    PaymentIntent intent = paymentIntentRepository.findById(intentId).orElseThrow(() -> new PaymentIntentNotFoundException(intentId));
    PayerLookupGateway.PayerInfo payer = payerLookupGateway.getPaymentProfile(intent.payerAccountId());
    return checkoutGateway.createPayment(intent, payer).invoiceUrl();
  }

  @Override
  @Transactional
  public PaymentIntent confirmPayment(UUID intentId, String externalPaymentId) {
    PaymentIntent intent = paymentIntentRepository.findById(intentId).orElseThrow(() -> new PaymentIntentNotFoundException(intentId));
    if (intent.status() == PaymentStatus.CAPTURED) {
      // Asaas retries webhook delivery — a retry for an already-settled intent must be a no-op,
      // not a second ledger credit.
      return intent;
    }
    Instant now = clock.instant();
    intent.authorize(externalPaymentId, now);
    paymentIntentRepository.save(intent);
    writeOutboxEvent(intent.id(), new PaymentAuthorized(intent.id(), intent.referenceId(), intent.purpose().code(), now));
    return capture(intentId);
  }

  @Override
  @Transactional
  public PaymentIntent capture(UUID intentId) {
    PaymentIntent intent = paymentIntentRepository.findById(intentId).orElseThrow(() -> new PaymentIntentNotFoundException(intentId));
    Instant now = clock.instant();
    intent.capture(now);
    paymentIntentRepository.save(intent);

    long payerBalance = ledgerEntryRepository.currentBalance(intent.payerAccountId()) - intent.amountCents();
    ledgerEntryRepository.save(
        LedgerEntry.record(intent.id(), intent.payerAccountId(), LedgerDirection.DEBIT, intent.amountCents(), payerBalance, now));
    long payeeBalance = ledgerEntryRepository.currentBalance(intent.payeeId()) + intent.amountCents();
    ledgerEntryRepository.save(LedgerEntry.record(intent.id(), intent.payeeId(), LedgerDirection.CREDIT, intent.amountCents(), payeeBalance, now));

    writeOutboxEvent(intent.id(), new PaymentCaptured(intent.id(), intent.referenceId(), intent.amountCents(), now));
    return intent;
  }

  @Override
  @Transactional
  public PaymentIntent refund(UUID intentId) {
    PaymentIntent intent = paymentIntentRepository.findById(intentId).orElseThrow(() -> new PaymentIntentNotFoundException(intentId));
    Instant now = clock.instant();
    intent.refund(now);
    paymentIntentRepository.save(intent);

    long payerBalance = ledgerEntryRepository.currentBalance(intent.payerAccountId()) + intent.amountCents();
    ledgerEntryRepository.save(
        LedgerEntry.record(intent.id(), intent.payerAccountId(), LedgerDirection.CREDIT, intent.amountCents(), payerBalance, now));
    long payeeBalance = ledgerEntryRepository.currentBalance(intent.payeeId()) - intent.amountCents();
    ledgerEntryRepository.save(LedgerEntry.record(intent.id(), intent.payeeId(), LedgerDirection.DEBIT, intent.amountCents(), payeeBalance, now));

    writeOutboxEvent(intent.id(), new PaymentRefunded(intent.id(), intent.referenceId(), intent.amountCents(), now));
    return intent;
  }

  @Override
  @Transactional(readOnly = true)
  public PaymentIntent getById(UUID intentId) {
    return paymentIntentRepository.findById(intentId).orElseThrow(() -> new PaymentIntentNotFoundException(intentId));
  }

  private void writeOutboxEvent(UUID aggregateId, DomainEvent event) {
    try {
      String payload = objectMapper.writeValueAsString(event);
      outboxEventRepository.save(OutboxEvent.record(UUID.randomUUID(), aggregateId, event.getClass().getSimpleName(), payload, event.occurredAt()));
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Failed to serialize outbox event " + event.getClass().getSimpleName(), e);
    }
  }
}
